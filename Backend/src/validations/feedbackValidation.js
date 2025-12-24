const Joi = require('joi')

const createFeedback = Joi.object().keys({
  fullName: Joi.string().trim().min(2).max(100).required().messages({
    'string.empty': 'Họ tên không được để trống',
    'string.min': 'Họ tên phải có ít nhất 2 ký tự',
    'string.max': 'Họ tên không được quá 100 ký tự',
    'any.required': 'Họ tên là bắt buộc'
  }),
  phoneNumber: Joi.string().pattern(/^[0-9]{10,11}$/).optional().messages({
    'string.pattern.base': 'Số điện thoại phải có 10-11 chữ số'
  }),
  email: Joi.string().email().optional().messages({
    'string.email': 'Email không hợp lệ'
  }),
  comment: Joi.string().trim().min(10).max(1000).required().messages({
    'string.empty': 'Bình luận không được để trống',
    'string.min': 'Bình luận phải có ít nhất 10 ký tự',
    'string.max': 'Bình luận không được quá 1000 ký tự',
    'any.required': 'Bình luận là bắt buộc'
  })
})

const updateFeedback = Joi.object().keys({
  comment: Joi.string().trim().min(10).max(1000).optional().messages({
    'string.min': 'Bình luận phải có ít nhất 10 ký tự',
    'string.max': 'Bình luận không được quá 1000 ký tự'
  })
})

const getFeedbacks = Joi.object().keys({
  page: Joi.number().integer().positive().default(1).messages({
    'number.base': 'Trang phải là số',
    'number.integer': 'Trang phải là số nguyên',
    'number.positive': 'Trang phải là số dương'
  }),
  limit: Joi.number().integer().positive().max(100).default(10).messages({
    'number.base': 'Số lượng phải là số',
    'number.integer': 'Số lượng phải là số nguyên',
    'number.positive': 'Số lượng phải là số dương',
    'number.max': 'Số lượng không được quá 100'
  }),
  sortBy: Joi.string().valid('createdAt', 'updatedAt').default('createdAt').messages({
    'any.only': 'Trường sắp xếp phải là createdAt hoặc updatedAt'
  }),
  sortOrder: Joi.string().valid('asc', 'desc').default('desc').messages({
    'any.only': 'Thứ tự sắp xếp phải là asc hoặc desc'
  })
})

const feedbackId = Joi.object().keys({
  id: Joi.number().integer().positive().required().messages({
    'number.base': 'ID phản hồi phải là số',
    'number.integer': 'ID phản hồi phải là số nguyên',
    'number.positive': 'ID phản hồi phải là số dương',
    'any.required': 'ID phản hồi là bắt buộc'
  })
})

module.exports = {
  createFeedback,
  updateFeedback,
  getFeedbacks,
  feedbackId
}
