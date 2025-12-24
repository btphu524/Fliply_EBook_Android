const axios = require('axios')
const EPub = require('epub')
const fs = require('fs')
const path = require('path')
const os = require('os')
const logger = require('../config/logger')

const tempDir = path.join(os.tmpdir(), 'reading-book-epub')

/**
 * Đảm bảo thư mục tạm tồn tại
 * @returns {void}
 */
const ensureTempDir = () => {
  if (!fs.existsSync(tempDir)) {
    fs.mkdirSync(tempDir, { recursive: true })
  }
}

ensureTempDir()

/**
 * Tạo tên file tạm thời
 * @returns {string} - Tên file tạm thời
 */
const generateTempFileName = () => {
  return `epub_${Date.now()}_${Math.random().toString(36).substr(2, 9)}.epub`
}

/**
 * Xóa file tạm thời
 * @param {string} filePath - Đường dẫn file cần xóa
 * @returns {void}
 */
const cleanupTempFile = (filePath) => {
  try {
    if (fs.existsSync(filePath)) {
      fs.unlinkSync(filePath)
    }
  } catch (error) {
    logger.error(`Failed to cleanup temp file: ${error.message}`)
  }
}

/**
 * Phân tích file EPUB từ URL
 * @param {Object} data - Dữ liệu yêu cầu
 * @param {string} data.url - URL của file EPUB
 * @returns {Promise<Object>} - Dữ liệu EPUB đã phân tích
 */
const parseEpubFromUrl = async (data) => {
  const { url } = data
  const tempPath = path.join(tempDir, generateTempFileName())

  try {
    const response = await axios({
      method: 'GET',
      url: url,
      responseType: 'arraybuffer',
      timeout: 30000,
      maxRedirects: 5,
      headers: {
        'User-Agent': 'Reading-Book-API/1.0'
      }
    })

    fs.writeFileSync(tempPath, response.data)

    const stats = fs.statSync(tempPath)
    if (stats.size === 0) {
      throw new Error('Downloaded file is empty')
    }

    const epubData = await parseEpubFile({ filePath: tempPath })

    return epubData

  } catch (error) {
    throw new Error(`Failed to process EPUB from URL: ${error.message}`)
  } finally {
    cleanupTempFile(tempPath)
  }
}

/**
 * @param {Object} data
 * @param {string} data.filePath
 * @return {Promise<Object>}
 */
const parseEpubFile = async (data) => {
  const { filePath } = data
  return new Promise((resolve, reject) => {
    const epub = new EPub(filePath)

    epub.on('end', () => {
      const result = {
        metadata: {
          title: epub.metadata.title || '',
          creator: epub.metadata.creator || '',
          publisher: epub.metadata.publisher || '',
          language: epub.metadata.language || 'en',
          description: epub.metadata.description || '',
          subject: epub.metadata.subject || '',
          date: epub.metadata.date || '',
          rights: epub.metadata.rights || ''
        },
        chapters: epub.flow.map(chapter => ({
          id: chapter.id,
          title: chapter.title,
          href: chapter.href,
          level: chapter.level || 1
        })),
        toc: epub.toc.map(item => ({
          id: item.id,
          title: item.title,
          href: item.href,
          level: item.level || 1
        })),
        manifest: Object.keys(epub.manifest).map(key => ({
          id: key,
          href: epub.manifest[key].href,
          mediaType: epub.manifest[key].mediaType
        })),
        spine: epub.spine,
        totalChapters: epub.flow.length
      }

      resolve(result)
    })

    epub.on('error', (err) => {
      reject(new Error(`Failed to parse EPUB: ${err.message}. This might be due to corrupted file, invalid EPUB format, or empty archive.`))
    })

    epub.parse()
  })
}

/**
 * @param {Object} data
 * @param {string} data.url
 * @return {Promise<Object>}
 */
const getEpubMetadata = async (data) => {
  const { url } = data
  try {
    const epubData = await parseEpubFromUrl({ url })
    return {
      success: true,
      data: {
        metadata: epubData.metadata,
        totalChapters: epubData.totalChapters,
        toc: epubData.toc
      }
    }
  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  }
}

/**
 * @param {Object} data
 * @param {string} data.url
 * @return {Promise<Object>}
 */
const getEpubChapters = async (data) => {
  const { url } = data
  try {
    const epubData = await parseEpubFromUrl({ url })
    return {
      success: true,
      data: {
        chapters: epubData.chapters,
        totalChapters: epubData.totalChapters
      }
    }
  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  }
}

/**
 * @param {Object} data
 * @param {string} data.url
 * @param {string} data.chapterId
 * @return {Promise<Object>}
 */
const getEpubChapterContent = async (data) => {
  const { url, chapterId } = data
  const tempPath = path.join(tempDir, generateTempFileName())

  try {
    const response = await axios({
      method: 'GET',
      url: url,
      responseType: 'arraybuffer',
      timeout: 30000,
      maxRedirects: 5
    })

    fs.writeFileSync(tempPath, response.data)

    const stats = fs.statSync(tempPath)
    if (stats.size === 0) {
      throw new Error('Downloaded file is empty')
    }

    const epub = new EPub(tempPath)

    return new Promise((resolve, reject) => {
      epub.on('end', () => {
        epub.getChapter(chapterId, (error, text) => {
          if (error) {
            reject(new Error(`Failed to get chapter content: ${error.message}`))
          } else {
            let chapterTitle = epub.flow.find(ch => ch.id === chapterId)?.title
            logger.debug(`🔍 [getChapterContent] Finding title for chapterId: ${chapterId}`)
            logger.debug(`📚 [getChapterContent] Flow chapters: ${JSON.stringify(epub.flow.map(ch => ({ id: ch.id, title: ch.title, href: ch.href })))}`)

            if (!chapterTitle) {
              logger.debug('❌ [getChapterContent] Not found in flow, searching TOC...')
              chapterTitle = epub.toc.find(item => item.id === chapterId)?.title
              logger.debug(`📖 [getChapterContent] TOC chapters: ${JSON.stringify(epub.toc.map(item => ({ id: item.id, title: item.title, href: item.href })))}`)
            }

            if (!chapterTitle) {
              logger.debug('❌ [getChapterContent] Not found by ID, searching by href...')
              const flowChapter = epub.flow.find(ch => ch.href === chapterId)
              if (flowChapter) {
                chapterTitle = flowChapter.title
                logger.debug(`✅ [getChapterContent] Found in flow by href: ${chapterTitle}`)
              } else {
                const tocChapter = epub.toc.find(item => item.href === chapterId)
                if (tocChapter) {
                  chapterTitle = tocChapter.title
                  logger.debug(`✅ [getChapterContent] Found in TOC by href: ${chapterTitle}`)
                }
              }
            } else {
              logger.debug(`✅ [getChapterContent] Found title: ${chapterTitle}`)
            }

            if (!chapterTitle) {
              chapterTitle = `Chapter ${chapterId}`
              logger.debug(`⚠️ [getChapterContent] Using fallback title: ${chapterTitle}`)
            }

            resolve({
              success: true,
              data: {
                chapterId,
                content: text,
                title: chapterTitle
              }
            })
          }
        })
      })

      epub.on('error', (err) => {
        reject(new Error(`Failed to parse EPUB: ${err.message}. This might be due to corrupted file, invalid EPUB format, or empty archive.`))
      })

      epub.parse()
    })

  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  } finally {
    cleanupTempFile(tempPath)
  }
}

/**
 * @param {Object} data
 * @param {string} data.url
 * @return {Promise<Object>}
 */
const validateEpubUrl = async (data) => {
  const { url } = data
  try {
    const urlPattern = /^https?:\/\/.+/
    if (!urlPattern.test(url)) {
      return {
        success: false,
        message: 'Invalid URL format'
      }
    }

    if (!url.toLowerCase().includes('.epub')) {
      return {
        success: false,
        message: 'URL does not point to an EPUB file'
      }
    }

    const response = await axios.head(url, {
      timeout: 10000,
      headers: {
        'User-Agent': 'Reading-Book-API/1.0'
      }
    })

    const contentType = response.headers['content-type']
    if (contentType && !contentType.includes('epub') && !contentType.includes('application/zip')) {
      return {
        success: false,
        message: 'File is not a valid EPUB format'
      }
    }

    return {
      success: true,
      message: 'Valid EPUB URL'
    }

  } catch (error) {
    return {
      success: false,
      message: `URL validation failed: ${error.message}`
    }
  }
}

/**
 * @param {Object} data
 * @param {string} data.url
 * @param {string} data.chapterId
 * @return {Promise<Object>}
 */
const getEpubChapterRaw = async (data) => {
  const { url, chapterId } = data
  const tempPath = path.join(tempDir, generateTempFileName())

  try {
    const response = await axios({
      method: 'GET',
      url: url,
      responseType: 'arraybuffer',
      timeout: 30000,
      maxRedirects: 5
    })

    fs.writeFileSync(tempPath, response.data)

    const stats = fs.statSync(tempPath)
    if (stats.size === 0) {
      throw new Error('Downloaded file is empty')
    }

    const epub = new EPub(tempPath)

    return new Promise((resolve, reject) => {
      epub.on('end', () => {
        epub.getChapterRaw(chapterId, (error, text) => {
          if (error) {
            reject(new Error(`Failed to get raw chapter content: ${error.message}`))
          } else {
            let chapterTitle = epub.flow.find(ch => ch.id === chapterId)?.title
            logger.debug(`🔍 [getChapterRaw] Finding title for chapterId: ${chapterId}`)
            logger.debug(`📚 [getChapterRaw] Flow chapters: ${JSON.stringify(epub.flow.map(ch => ({ id: ch.id, title: ch.title, href: ch.href })))}`)
            if (!chapterTitle) {
              logger.debug('❌ [getChapterRaw] Not found in flow, searching TOC...')
              chapterTitle = epub.toc.find(item => item.id === chapterId)?.title
              logger.debug(`📖 [getChapterRaw] TOC chapters: ${JSON.stringify(epub.toc.map(item => ({ id: item.id, title: item.title, href: item.href })))}`)
            }
            if (!chapterTitle) {
              logger.debug('❌ [getChapterRaw] Not found by ID, searching by href...')
              const flowChapter = epub.flow.find(ch => ch.href === chapterId)
              if (flowChapter) {
                chapterTitle = flowChapter.title
                logger.debug(`✅ [getChapterRaw] Found in flow by href: ${chapterTitle}`)
              } else {
                const tocChapter = epub.toc.find(item => item.href === chapterId)
                if (tocChapter) {
                  chapterTitle = tocChapter.title
                  logger.debug(`✅ [getChapterRaw] Found in TOC by href: ${chapterTitle}`)
                }
              }
            } else {
              logger.debug(`✅ [getChapterRaw] Found title: ${chapterTitle}`)
            }
            if (!chapterTitle) {
              chapterTitle = `Chapter ${chapterId}`
              logger.debug(`⚠️ [getChapterRaw] Using fallback title: ${chapterTitle}`)
            }
            resolve({
              success: true,
              data: {
                chapterId,
                rawContent: text,
                title: chapterTitle
              }
            })
          }
        })
      })

      epub.on('error', (err) => {
        reject(new Error(`Failed to parse EPUB: ${err.message}. This might be due to corrupted file, invalid EPUB format, or empty archive.`))
      })

      epub.parse()
    })

  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  } finally {
    cleanupTempFile(tempPath)
  }
}

/**
 * @param {Object} data
 * @param {string} data.url
 * @param {string} data.imageId
 * @return {Promise<Object>}
 */
const getEpubImage = async (data) => {
  const { url, imageId } = data
  const tempPath = path.join(tempDir, generateTempFileName())

  try {
    const response = await axios({
      method: 'GET',
      url: url,
      responseType: 'arraybuffer',
      timeout: 30000,
      maxRedirects: 5
    })

    fs.writeFileSync(tempPath, response.data)

    const stats = fs.statSync(tempPath)
    if (stats.size === 0) {
      throw new Error('Downloaded file is empty')
    }

    const epub = new EPub(tempPath)

    return new Promise((resolve, reject) => {
      epub.on('end', () => {
        epub.getImage(imageId, (error, img, mimeType) => {
          if (error) {
            reject(new Error(`Failed to get image: ${error.message}`))
          } else {
            resolve({
              success: true,
              data: {
                imageId,
                image: img,
                mimeType: mimeType,
                base64: `data:${mimeType};base64,${img.toString('base64')}`
              }
            })
          }
        })
      })

      epub.on('error', (err) => {
        reject(new Error(`Failed to parse EPUB: ${err.message}. This might be due to corrupted file, invalid EPUB format, or empty archive.`))
      })

      epub.parse()
    })

  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  } finally {
    cleanupTempFile(tempPath)
  }
}

/**
 * @param {Object} data
 * @param {string} data.url
 * @param {string} data.fileId
 * @return {Promise<Object>}
 */
const getEpubFile = async (data) => {
  const { url, fileId } = data
  const tempPath = path.join(tempDir, generateTempFileName())

  try {
    const response = await axios({
      method: 'GET',
      url: url,
      responseType: 'arraybuffer',
      timeout: 30000,
      maxRedirects: 5
    })

    fs.writeFileSync(tempPath, response.data)

    const stats = fs.statSync(tempPath)
    if (stats.size === 0) {
      throw new Error('Downloaded file is empty')
    }

    const epub = new EPub(tempPath)

    return new Promise((resolve, reject) => {
      epub.on('end', () => {
        epub.getFile(fileId, (error, data, mimeType) => {
          if (error) {
            reject(new Error(`Failed to get file: ${error.message}`))
          } else {
            resolve({
              success: true,
              data: {
                fileId,
                content: data,
                mimeType: mimeType,
                size: data.length
              }
            })
          }
        })
      })

      epub.on('error', (err) => {
        reject(new Error(`Failed to parse EPUB: ${err.message}. This might be due to corrupted file, invalid EPUB format, or empty archive.`))
      })

      epub.parse()
    })

  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  } finally {
    cleanupTempFile(tempPath)
  }
}

/**
 * @param {Object} data
 * @param {string} data.url
 * @return {Promise<Object>}
 */
const getEpubImages = async (data) => {
  const { url } = data
  try {
    const epubData = await parseEpubFromUrl({ url })
    const images = epubData.manifest.filter(item =>
      item.mediaType && item.mediaType.startsWith('image/')
    )

    return {
      success: true,
      data: {
        images: images,
        totalImages: images.length
      }
    }
  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  }
}

/**
 * @return {void}
 */
const cleanupOldTempFiles = () => {
  try {
    const files = fs.readdirSync(tempDir)
    const oneHourAgo = Date.now() - (60 * 60 * 1000)

    files.forEach(file => {
      const filePath = path.join(tempDir, file)
      const stats = fs.statSync(filePath)

      if (stats.mtime.getTime() < oneHourAgo) {
        fs.unlinkSync(filePath)
      }
    })
  } catch (error) {
    logger.error(`Failed to cleanup old temp files: ${error.message}`)
  }
}


module.exports = {
  parseEpubFromUrl,
  parseEpubFile,
  getEpubMetadata,
  getEpubChapters,
  getEpubChapterContent,
  validateEpubUrl,
  getEpubChapterRaw,
  getEpubImage,
  getEpubFile,
  getEpubImages,
  cleanupOldTempFiles
}
