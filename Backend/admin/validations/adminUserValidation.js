const Joi = require('joi')

/**
 * Tạo user mới
 */
const createUser = {
  body: Joi.object().keys({
    email: Joi.string().email().required().trim().lowercase().messages({
      'string.email': 'Email không hợp lệ',
      'string.empty': 'Email không được để trống',
      'any.required': 'Email là bắt buộc'
    }),
    password: Joi.string().min(6).max(128).required().messages({
      'string.min': 'Mật khẩu phải có ít nhất 6 ký tự',
      'string.max': 'Mật khẩu không được vượt quá 128 ký tự',
      'string.empty': 'Mật khẩu không được để trống',
      'any.required': 'Mật khẩu là bắt buộc'
    }),
    confirmPassword: Joi.string().valid(Joi.ref('password')).required().messages({
      'any.only': 'Xác nhận mật khẩu không khớp',
      'any.required': 'Xác nhận mật khẩu là bắt buộc'
    }),
    fullName: Joi.string().required().trim().min(1).max(255).messages({
      'string.empty': 'Họ tên không được để trống',
      'string.min': 'Họ tên phải có ít nhất 1 ký tự',
      'string.max': 'Họ tên không được vượt quá 255 ký tự',
      'any.required': 'Họ tên là bắt buộc'
    }),
    phoneNumber: Joi.string().pattern(/^[0-9+\-\s()]+$/).required().trim().min(10).max(15).messages({
      'string.pattern.base': 'Số điện thoại không hợp lệ',
      'string.min': 'Số điện thoại phải có ít nhất 10 ký tự',
      'string.max': 'Số điện thoại không được vượt quá 15 ký tự',
      'string.empty': 'Số điện thoại không được để trống',
      'any.required': 'Số điện thoại là bắt buộc'
    }),
    role: Joi.string().valid('user', 'admin').default('user').messages({
      'any.only': 'Vai trò phải là user hoặc admin'
    }),
    isActive: Joi.boolean().default(true).messages({
      'boolean.base': 'Trạng thái hoạt động phải là true hoặc false'
    })
  }).unknown(false).messages({
    'object.unknown': 'Trường {#label} không được phép. Vui lòng không gửi các trường hệ thống như _id, createdAt, updatedAt'
  })
}

/**
 * Validation cho xóa user (admin)
 */
const deleteUser = {
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
 * Validation cho xóa vĩnh viễn user (admin)
 */
const hardDeleteUser = {
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
 * Validation cho khôi phục user (admin)
 */
const restoreUser = {
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
 * Validation cho lấy danh sách users đã xóa (admin)
 */
const getDeletedUsers = {
  query: Joi.object().keys({
    page: Joi.number().integer().min(1).default(1).messages({
      'number.base': 'Số trang phải là số nguyên',
      'number.integer': 'Số trang phải là số nguyên',
      'number.min': 'Số trang phải lớn hơn 0'
    }),
    limit: Joi.number().integer().min(1).max(100).default(10).messages({
      'number.base': 'Số lượng mỗi trang phải là số nguyên',
      'number.integer': 'Số lượng mỗi trang phải là số nguyên',
      'number.min': 'Số lượng mỗi trang phải lớn hơn 0',
      'number.max': 'Số lượng mỗi trang không được vượt quá 100'
    }),
    sortBy: Joi.string().valid('createdAt', 'updatedAt', 'deletedAt', 'fullName', 'email').default('deletedAt').messages({
      'any.only': 'Sắp xếp theo phải là createdAt, updatedAt, deletedAt, fullName hoặc email'
    }),
    sortOrder: Joi.string().valid('asc', 'desc').default('desc').messages({
      'any.only': 'Thứ tự sắp xếp phải là asc hoặc desc'
    }),
    role: Joi.string().valid('user', 'admin').messages({
      'any.only': 'Vai trò phải là user hoặc admin'
    })
  })
}

/**
 * Validation cho lấy thống kê users (admin)
 */
const getUserStats = {
  query: Joi.object().keys({
    period: Joi.string().valid('day', 'week', 'month', 'year', 'all').default('all').messages({
      'any.only': 'Khoảng thời gian phải là day, week, month, year hoặc all'
    })
  })
}

module.exports = {
  createUser,
  deleteUser,
  hardDeleteUser,
  restoreUser,
  getDeletedUsers,
  getUserStats
}
