const Joi = require('joi')

/**
 * Validation schemas for EPUB operations
 * @namespace epubValidation
 */
const epubValidation = {
  /**
   * @param {Object} body - Request body
   * @param {string} body.epub_url - EPUB file URL (valid URI)
   * @return {Object} Joi validation schema
   */
  getMetadata: {
    body: Joi.object().keys({
      epub_url: Joi.string().required().uri().messages({
        'string.uri': 'URL EPUB không hợp lệ',
        'any.required': 'URL EPUB là bắt buộc'
      })
    })
  },

  /**
   * @param {Object} body - Request body
   * @param {string} body.epub_url - EPUB file URL (valid URI)
   * @return {Object} Joi validation schema
   */
  getChapters: {
    body: Joi.object().keys({
      epub_url: Joi.string().required().uri().messages({
        'string.uri': 'URL EPUB không hợp lệ',
        'any.required': 'URL EPUB là bắt buộc'
      })
    })
  },

  /**
   * @param {Object} body - Request body
   * @param {string} body.epub_url - EPUB file URL (valid URI)
   * @param {string} body.chapter_id - Chapter ID (non-empty string)
   * @return {Object} Joi validation schema
   */
  getChapterContent: {
    body: Joi.object().keys({
      epub_url: Joi.string().required().uri().messages({
        'string.uri': 'URL EPUB không hợp lệ',
        'any.required': 'URL EPUB là bắt buộc'
      }),
      chapter_id: Joi.string().required().trim().min(1).messages({
        'string.empty': 'ID chương không được để trống',
        'any.required': 'ID chương là bắt buộc',
        'string.min': 'ID chương phải có ít nhất 1 ký tự'
      })
    })
  },

  /**
   * @param {Object} body - Request body
   * @param {string} body.epub_url - EPUB file URL (valid URI)
   * @return {Object} Joi validation schema
   */
  validateUrl: {
    body: Joi.object().keys({
      epub_url: Joi.string().required().uri().messages({
        'string.uri': 'URL EPUB không hợp lệ',
        'any.required': 'URL EPUB là bắt buộc'
      })
    })
  },

  /**
   * @param {Object} body - Request body
   * @param {string} body.epub_url - EPUB file URL (valid URI)
   * @param {string} body.chapter_id - Chapter ID (non-empty string)
   * @return {Object} Joi validation schema
   */
  getChapterRaw: {
    body: Joi.object().keys({
      epub_url: Joi.string().required().uri().messages({
        'string.uri': 'URL EPUB không hợp lệ',
        'any.required': 'URL EPUB là bắt buộc'
      }),
      chapter_id: Joi.string().required().trim().min(1).messages({
        'string.empty': 'ID chương không được để trống',
        'any.required': 'ID chương là bắt buộc',
        'string.min': 'ID chương phải có ít nhất 1 ký tự'
      })
    })
  },

  /**
   * @param {Object} body - Request body
   * @param {string} body.epub_url - EPUB file URL (valid URI)
   * @param {string} body.image_id - Image ID (non-empty string)
   * @return {Object} Joi validation schema
   */
  getImage: {
    body: Joi.object().keys({
      epub_url: Joi.string().required().uri().messages({
        'string.uri': 'URL EPUB không hợp lệ',
        'any.required': 'URL EPUB là bắt buộc'
      }),
      image_id: Joi.string().required().trim().min(1).messages({
        'string.empty': 'ID ảnh không được để trống',
        'any.required': 'ID ảnh là bắt buộc',
        'string.min': 'ID ảnh phải có ít nhất 1 ký tự'
      })
    })
  },

  /**
   * @param {Object} body - Request body
   * @param {string} body.epub_url - EPUB file URL (valid URI)
   * @param {string} body.file_id - File ID (non-empty string)
   * @return {Object} Joi validation schema
   */
  getFile: {
    body: Joi.object().keys({
      epub_url: Joi.string().required().uri().messages({
        'string.uri': 'URL EPUB không hợp lệ',
        'any.required': 'URL EPUB là bắt buộc'
      }),
      file_id: Joi.string().required().trim().min(1).messages({
        'string.empty': 'ID file không được để trống',
        'any.required': 'ID file là bắt buộc',
        'string.min': 'ID file phải có ít nhất 1 ký tự'
      })
    })
  },

  /**
   * @param {Object} body - Request body
   * @param {string} body.epub_url - EPUB file URL (valid URI)
   * @return {Object} Joi validation schema
   */
  getImages: {
    body: Joi.object().keys({
      epub_url: Joi.string().required().uri().messages({
        'string.uri': 'URL EPUB không hợp lệ',
        'any.required': 'URL EPUB là bắt buộc'
      })
    })
  }
}

module.exports = epubValidation
