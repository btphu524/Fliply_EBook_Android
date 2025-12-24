const {
  getEpubMetadata,
  getEpubChapters,
  getEpubChapterContent,
  validateEpubUrl,
  getEpubChapterRaw,
  getEpubImage,
  getEpubFile,
  getEpubImages
} = require('../services/epubService')
const logger = require('../config/logger')

const epubController = {
  /**
   * Lấy metadata EPUB từ URL
   * @param {Object} req - HTTP request (body: { epub_url })
   * @param {Object} res - HTTP response
   */
  getMetadata: async (req, res) => {
    try {
      const { epub_url } = req.body

      logger.info(`📖 EPUB Metadata Request - URL: ${epub_url}`)

      if (!epub_url) {
        logger.warn('❌ EPUB Metadata Request failed - Missing epub_url')
        return res.status(400).json({
          success: false,
          message: 'epub_url is required'
        })
      }

      const result = await getEpubMetadata({ url: epub_url })

      if (result.success) {
        logger.info(`✅ EPUB Metadata Request successful - Title: ${result.data?.metadata?.title || 'Unknown'}`)
        res.status(200).json(result)
      } else {
        logger.warn(`❌ EPUB Metadata Request failed - ${result.message}`)
        res.status(400).json(result)
      }
    } catch (error) {
      logger.error(`💥 EPUB Metadata Request error: ${error.message}`)
      res.status(500).json({
        success: false,
        message: `Lỗi server: ${error.message}`
      })
    }
  },

  /**
   * Lấy danh sách chương EPUB từ URL
   * @param {Object} req - HTTP request (body: { epub_url })
   * @param {Object} res - HTTP response
   */
  getChapters: async (req, res) => {
    try {
      const { epub_url } = req.body

      if (!epub_url) {
        return res.status(400).json({
          success: false,
          message: 'epub_url is required'
        })
      }

      const result = await getEpubChapters({ url: epub_url })

      if (result.success) {
        res.status(200).json(result)
      } else {
        res.status(400).json(result)
      }
    } catch (error) {
      res.status(500).json({
        success: false,
        message: `Lỗi server: ${error.message}`
      })
    }
  },

  /**
   * Lấy nội dung chương EPUB từ URL
   * @param {Object} req - HTTP request (body: { epub_url, chapter_id })
   * @param {Object} res - HTTP response
   */
  getChapterContent: async (req, res) => {
    try {
      const { epub_url, chapter_id } = req.body

      if (!epub_url || !chapter_id) {
        return res.status(400).json({
          success: false,
          message: 'epub_url and chapter_id are required'
        })
      }

      const result = await getEpubChapterContent({ url: epub_url, chapterId: chapter_id })

      if (result.success) {
        res.status(200).json(result)
      } else {
        res.status(400).json(result)
      }
    } catch (error) {
      res.status(500).json({
        success: false,
        message: `Lỗi server: ${error.message}`
      })
    }
  },

  /**
   * Validate EPUB URL
   * @param {Object} req - HTTP request (body: { epub_url })
   * @param {Object} res - HTTP response
   */
  validateUrl: async (req, res) => {
    try {
      const { epub_url } = req.body

      if (!epub_url) {
        return res.status(400).json({
          success: false,
          message: 'epub_url is required'
        })
      }

      const result = await validateEpubUrl({ url: epub_url })

      if (result.success) {
        res.status(200).json(result)
      } else {
        res.status(400).json(result)
      }
    } catch (error) {
      res.status(500).json({
        success: false,
        message: `Lỗi server: ${error.message}`
      })
    }
  },

  /**
   * Lấy nội dung chương dạng raw
   * @param {Object} req - HTTP request (body: { epub_url, chapter_id })
   * @param {Object} res - HTTP response
   */
  getChapterRaw: async (req, res) => {
    try {
      const { epub_url, chapter_id } = req.body

      if (!epub_url || !chapter_id) {
        return res.status(400).json({
          success: false,
          message: 'epub_url and chapter_id are required'
        })
      }

      const result = await getEpubChapterRaw({ url: epub_url, chapterId: chapter_id })

      if (result.success) {
        res.status(200).json(result)
      } else {
        res.status(400).json(result)
      }
    } catch (error) {
      res.status(500).json({
        success: false,
        message: `Lỗi server: ${error.message}`
      })
    }
  },

  /**
   * Lấy ảnh từ EPUB
   * @param {Object} req - HTTP request (body: { epub_url, image_id })
   * @param {Object} res - HTTP response
   */
  getImage: async (req, res) => {
    try {
      const { epub_url, image_id } = req.body

      if (!epub_url || !image_id) {
        return res.status(400).json({
          success: false,
          message: 'epub_url and image_id are required'
        })
      }

      const result = await getEpubImage({ url: epub_url, imageId: image_id })

      if (result.success) {
        res.status(200).json(result)
      } else {
        res.status(400).json(result)
      }
    } catch (error) {
      res.status(500).json({
        success: false,
        message: `Lỗi server: ${error.message}`
      })
    }
  },

  /**
   * Lấy file từ EPUB
   * @param {Object} req - HTTP request (body: { epub_url, file_id })
   * @param {Object} res - HTTP response
   */
  getFile: async (req, res) => {
    try {
      const { epub_url, file_id } = req.body

      if (!epub_url || !file_id) {
        return res.status(400).json({
          success: false,
          message: 'epub_url and file_id are required'
        })
      }

      const result = await getEpubFile({ url: epub_url, fileId: file_id })

      if (result.success) {
        res.status(200).json(result)
      } else {
        res.status(400).json(result)
      }
    } catch (error) {
      res.status(500).json({
        success: false,
        message: `Lỗi server: ${error.message}`
      })
    }
  },

  /**
   * Lấy danh sách ảnh trong EPUB
   * @param {Object} req - HTTP request (body: { epub_url })
   * @param {Object} res - HTTP response
   */
  getImages: async (req, res) => {
    try {
      const { epub_url } = req.body

      if (!epub_url) {
        return res.status(400).json({
          success: false,
          message: 'epub_url is required'
        })
      }

      const result = await getEpubImages({ url: epub_url })

      if (result.success) {
        res.status(200).json(result)
      } else {
        res.status(400).json(result)
      }
    } catch (error) {
      res.status(500).json({
        success: false,
        message: `Lỗi server: ${error.message}`
      })
    }
  }
}

module.exports = epubController
