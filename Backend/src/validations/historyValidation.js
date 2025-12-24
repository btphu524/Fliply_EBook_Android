const Joi = require('joi')

const historyValidation = {
  /**
   * @param {Object} body - Request body
   * @return {Object} Joi validation schema
   */
  saveBookmark: {
    body: Joi.object().keys({
      userId: Joi.number().integer().positive().required().messages({
        'number.base': 'ID người dùng phải là số',
        'number.integer': 'ID người dùng phải là số nguyên',
        'number.positive': 'ID người dùng phải là số dương',
        'any.required': 'ID người dùng là bắt buộc'
      }),
      bookId: Joi.number().integer().positive().required().messages({
        'number.base': 'ID sách phải là số',
        'number.integer': 'ID sách phải là số nguyên',
        'number.positive': 'ID sách phải là số dương',
        'any.required': 'ID sách là bắt buộc'
      }),
      chapterId: Joi.string().required().messages({
        'string.base': 'ID chương phải là chuỗi',
        'any.required': 'ID chương là bắt buộc'
      })
    })
  },

  /**
   * @param {Object} params - URL parameters
   * @param {Object} query - Query parameters
   * @return {Object} Joi validation schema
   */
  getReadingHistory: {
    params: Joi.object().keys({
      userId: Joi.number().integer().positive().required().messages({
        'number.base': 'ID người dùng phải là số',
        'number.integer': 'ID người dùng phải là số nguyên',
        'number.positive': 'ID người dùng phải là số dương',
        'any.required': 'ID người dùng là bắt buộc'
      })
    }),
    query: Joi.object().keys({
      page: Joi.number().integer().min(1).optional().messages({
        'number.base': 'Trang phải là số',
        'number.integer': 'Trang phải là số nguyên',
        'number.min': 'Trang phải lớn hơn hoặc bằng 1'
      }),
      limit: Joi.number().integer().min(1).max(100).optional().messages({
        'number.base': 'Giới hạn phải là số',
        'number.integer': 'Giới hạn phải là số nguyên',
        'number.min': 'Giới hạn phải lớn hơn hoặc bằng 1',
        'number.max': 'Giới hạn phải nhỏ hơn hoặc bằng 100'
      }),
      sortBy: Joi.string().valid('lastReadAt', 'createdAt', 'page').optional().messages({
        'any.only': 'Trường sắp xếp phải là một trong: lastReadAt, createdAt, page'
      }),
      sortOrder: Joi.string().valid('asc', 'desc').optional().messages({
        'any.only': 'Thứ tự sắp xếp phải là asc hoặc desc'
      })
    })
  },

  /**
   * @param {Object} params - URL parameters
   * @return {Object} Joi validation schema
   */
  getBookmark: {
    params: Joi.object().keys({
      userId: Joi.number().integer().positive().required().messages({
        'number.base': 'ID người dùng phải là số',
        'number.integer': 'ID người dùng phải là số nguyên',
        'number.positive': 'ID người dùng phải là số dương',
        'any.required': 'ID người dùng là bắt buộc'
      }),
      bookId: Joi.number().integer().positive().required().messages({
        'number.base': 'ID sách phải là số',
        'number.integer': 'ID sách phải là số nguyên',
        'number.positive': 'ID sách phải là số dương',
        'any.required': 'ID sách là bắt buộc'
      })
    })
  },

  /**
   * @param {Object} params - URL parameters
   * @return {Object} Joi validation schema
   */
  deleteBookmark: {
    params: Joi.object().keys({
      userId: Joi.number().integer().positive().required().messages({
        'number.base': 'ID người dùng phải là số',
        'number.integer': 'ID người dùng phải là số nguyên',
        'number.positive': 'ID người dùng phải là số dương',
        'any.required': 'ID người dùng là bắt buộc'
      }),
      bookId: Joi.number().integer().positive().required().messages({
        'number.base': 'ID sách phải là số',
        'number.integer': 'ID sách phải là số nguyên',
        'number.positive': 'ID sách phải là số dương',
        'any.required': 'ID sách là bắt buộc'
      })
    })
  },


  /**
   * @param {Object} params - URL parameters
   * @return {Object} Joi validation schema
   */
  getHistoryByUser: {
    params: Joi.object().keys({
      userId: Joi.number().integer().positive().required().messages({
        'number.base': 'ID người dùng phải là số',
        'number.integer': 'ID người dùng phải là số nguyên',
        'number.positive': 'ID người dùng phải là số dương',
        'any.required': 'ID người dùng là bắt buộc'
      })
    })
  },

  /**
   * @param {Object} params - URL parameters
   * @return {Object} Joi validation schema
   */
  getHistoryByBook: {
    params: Joi.object().keys({
      bookId: Joi.number().integer().positive().required().messages({
        'number.base': 'ID sách phải là số',
        'number.integer': 'ID sách phải là số nguyên',
        'number.positive': 'ID sách phải là số dương',
        'any.required': 'ID sách là bắt buộc'
      })
    })
  }
}

module.exports = historyValidation
