const Joi = require('joi')

/**
 * Validation cho lấy thể loại theo ID
 * @param {Object} params - Route parameters
 * @param {string} params.categoryId - Category ID (positive integer)
 * @return {Object} Joi validation schema
 */
const getById = {
  params: Joi.object().keys({
    categoryId: Joi.number().integer().positive().required().messages({
      'number.base': 'ID thể loại phải là số nguyên',
      'number.integer': 'ID thể loại phải là số nguyên',
      'number.positive': 'ID thể loại phải là số dương',
      'any.required': 'ID thể loại là bắt buộc'
    })
  })
}

module.exports = {
  getById
}
