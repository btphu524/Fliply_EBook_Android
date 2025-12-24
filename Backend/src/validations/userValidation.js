const Joi = require('joi')

/**
 * Validation cho lấy thông tin user theo email
 * @param {Object} query - Query parameters
 * @param {string} query.email - User email
 * @return {Object} Joi validation schema
 */
const getUser = {
  query: Joi.object().keys({
    email: Joi.string().email().required().messages({
      'string.email': 'Email không hợp lệ',
      'any.required': 'Email là bắt buộc'
    })
  })
}

/**
 * Validation cho lấy thông tin user theo ID
 * @param {Object} params - Route parameters
 * @param {string} params.userId - User ID (positive integer)
 * @return {Object} Joi validation schema
 */
const getUserById = {
  params: Joi.object().keys({
    userId: Joi.number().integer().positive().required().messages({
      'number.base': 'ID user phải là số nguyên',
      'number.integer': 'ID user phải là số nguyên',
      'number.positive': 'ID user phải là số dương',
      'any.required': 'ID user là bắt buộc'
    })
  })
}

/**
 * Validation cho cập nhật thông tin user
 * @param {Object} params - Route parameters
 * @param {string} params.userId - User ID (positive integer)
 * @param {Object} body - Request body
 * @param {string} [body.fullName] - User full name
 * @param {string} [body.phoneNumber] - User phone number
 * @param {string} [body.avatar] - User avatar URL
 * @return {Object} Joi validation schema
 */
const updateUser = {
  params: Joi.object().keys({
    userId: Joi.number().integer().positive().required().messages({
      'number.base': 'ID user phải là số nguyên',
      'number.integer': 'ID user phải là số nguyên',
      'number.positive': 'ID user phải là số dương',
      'any.required': 'ID user là bắt buộc'
    })
  }),
  body: Joi.object().keys({
    fullName: Joi.string().trim().min(1).max(255).messages({
      'string.min': 'Họ tên phải có ít nhất 1 ký tự',
      'string.max': 'Họ tên không được vượt quá 255 ký tự'
    }),
    phoneNumber: Joi.string().pattern(/^[0-9+\-\s()]+$/).messages({
      'string.pattern.base': 'Số điện thoại không hợp lệ'
    }),
    avatar: Joi.string().uri().messages({
      'string.uri': 'URL avatar không hợp lệ'
    })
  }).min(1).messages({
    'object.min': 'Phải cung cấp ít nhất một trường để cập nhật'
  })
}

/**
 * Validation cho lấy danh sách sách yêu thích
 * @param {Object} params - Route parameters
 * @param {string} params.userId - User ID (positive integer)
 * @return {Object} Joi validation schema
 */
const getFavoriteBooks = {
  params: Joi.object().keys({
    userId: Joi.number().integer().positive().required().messages({
      'number.base': 'ID user phải là số nguyên',
      'number.integer': 'ID user phải là số nguyên',
      'number.positive': 'ID user phải là số dương',
      'any.required': 'ID user là bắt buộc'
    })
  })
}

/**
 * Validation cho thêm sách vào danh sách yêu thích
 * @param {Object} params - Route parameters
 * @param {string} params.userId - User ID (positive integer)
 * @param {string} params.bookId - Book ID (positive integer)
 * @return {Object} Joi validation schema
 */
const addFavoriteBook = {
  params: Joi.object().keys({
    userId: Joi.number().integer().positive().required().messages({
      'number.base': 'ID user phải là số nguyên',
      'number.integer': 'ID user phải là số nguyên',
      'number.positive': 'ID user phải là số dương',
      'any.required': 'ID user là bắt buộc'
    }),
    bookId: Joi.number().integer().positive().required().messages({
      'number.base': 'ID sách phải là số nguyên',
      'number.integer': 'ID sách phải là số nguyên',
      'number.positive': 'ID sách phải là số dương',
      'any.required': 'ID sách là bắt buộc'
    })
  })
}

/**
 * Validation cho xóa sách khỏi danh sách yêu thích
 * @param {Object} params - Route parameters
 * @param {string} params.userId - User ID (positive integer)
 * @param {string} params.bookId - Book ID (positive integer)
 * @return {Object} Joi validation schema
 */
const removeFavoriteBook = {
  params: Joi.object().keys({
    userId: Joi.number().integer().positive().required().messages({
      'number.base': 'ID user phải là số nguyên',
      'number.integer': 'ID user phải là số nguyên',
      'number.positive': 'ID user phải là số dương',
      'any.required': 'ID user là bắt buộc'
    }),
    bookId: Joi.number().integer().positive().required().messages({
      'number.base': 'ID sách phải là số nguyên',
      'number.integer': 'ID sách phải là số nguyên',
      'number.positive': 'ID sách phải là số dương',
      'any.required': 'ID sách là bắt buộc'
    })
  })
}

module.exports = {
  getUser,
  getUserById,
  updateUser,
  getFavoriteBooks,
  addFavoriteBook,
  removeFavoriteBook
}
