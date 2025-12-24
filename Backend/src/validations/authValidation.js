const Joi = require('joi')
const { password, confirmPassword } = require('./custom')

/**
 * Validation schemas for authentication operations
 * @namespace authValidation
 */
const authValidation = {
  /**
   * Validation schema cho đăng ký người dùng
   * @param {Object} body - Request body
   * @param {string} body.email - Email address (valid email format)
   * @param {string} body.password - Password (custom validation)
   * @param {string} body.confirmPassword - Password confirmation (must match password)
   * @param {string} body.fullName - Full name (required)
   * @param {string} body.phoneNumber - Phone number (10-11 digits)
   * @param {string} [body.role='user'] - User role ('user' or 'admin')
   * @param {number} [body._id] - User ID (positive integer)
   * @param {number} [body.userId] - User ID (positive integer)
   * @returns {Object} Joi validation schema
   */
  register: {
    body: Joi.object().keys({
      email: Joi.string().required().email().messages({
        'string.email': 'Email không hợp lệ',
        'any.required': 'Email là bắt buộc'
      }),
      password: Joi.string().required().custom(password).messages({
        'any.required': 'Mật khẩu là bắt buộc'
      }),
      confirmPassword: Joi.string().required().custom(confirmPassword).messages({
        'any.required': 'Xác nhận mật khẩu là bắt buộc'
      }),
      fullName: Joi.string().required().messages({
        'any.required': 'Họ tên là bắt buộc'
      }),
      phoneNumber: Joi.string()
        .required()
        .pattern(/^[0-9]{10,11}$/)
        .messages({
          'any.required': 'Số điện thoại là bắt buộc',
          'string.pattern.base': 'Số điện thoại phải có 10-11 chữ số'
        }),
      role: Joi.string().valid('user', 'admin').default('user').messages({
        'any.only': 'Vai trò phải là user hoặc admin'
      }),
      _id: Joi.number().integer().positive().optional(),
      userId: Joi.number().integer().positive().optional()
    })
  },

  /**
   * Validation schema cho xác thực OTP
   * @param {Object} body - Request body
   * @param {string} body.email - Email address (valid email format)
   * @param {string} body.otp - OTP code (required)
   * @returns {Object} Joi validation schema
   */
  verifyOTP: {
    body: Joi.object().keys({
      email: Joi.string().required().email().messages({
        'string.email': 'Email không hợp lệ',
        'any.required': 'Email là bắt buộc'
      }),
      otp: Joi.string().required().messages({
        'any.required': 'Mã OTP là bắt buộc'
      })
    })
  },

  /**
   * Validation schema cho gửi lại OTP
   * @param {Object} body - Request body
   * @param {string} body.email - Email address (valid email format)
   * @returns {Object} Joi validation schema
   */
  resendOTP: {
    body: Joi.object().keys({
      email: Joi.string().required().email().messages({
        'string.email': 'Email không hợp lệ',
        'any.required': 'Email là bắt buộc'
      })
    })
  },

  /**
   * Validation schema cho đăng nhập
   * @param {Object} body - Request body
   * @param {string} body.email - Email address (valid email format)
   * @param {string} body.password - Password (required)
   * @returns {Object} Joi validation schema
   */
  login: {
    body: Joi.object().keys({
      email: Joi.string().required().email().messages({
        'string.email': 'Email không hợp lệ',
        'any.required': 'Email là bắt buộc'
      }),
      password: Joi.string().required().messages({
        'any.required': 'Mật khẩu là bắt buộc'
      })
    })
  },

  /**
   * Validation schema cho quên mật khẩu
   * @param {Object} body - Request body
   * @param {string} body.email - Email address (valid email format)
   * @returns {Object} Joi validation schema
   */
  forgotPassword: {
    body: Joi.object().keys({
      email: Joi.string().required().email().messages({
        'string.email': 'Email không hợp lệ',
        'any.required': 'Email là bắt buộc'
      })
    })
  },

  /**
   * Validation schema cho đặt lại mật khẩu
   * @param {Object} body - Request body
   * @param {string} body.email - Email address (valid email format)
   * @param {string} body.newPassword - New password (custom validation)
   * @param {string} body.confirmPassword - Password confirmation (must match newPassword)
   * @returns {Object} Joi validation schema
   */
  resetPassword: {
    body: Joi.object().keys({
      email: Joi.string().required().email().messages({
        'string.email': 'Email không hợp lệ',
        'any.required': 'Email là bắt buộc'
      }),
      newPassword: Joi.string().required().custom(password).messages({
        'any.required': 'Mật khẩu mới là bắt buộc'
      }),
      confirmPassword: Joi.string().required().custom(confirmPassword).messages({
        'any.required': 'Xác nhận mật khẩu là bắt buộc'
      })
    })
  },

  /**
   * Validation schema cho đổi mật khẩu
   * @param {Object} body - Request body
   * @param {string} body.oldPassword - Mật khẩu cũ (required)
   * @param {string} body.newPassword - Mật khẩu mới (custom validation)
   * @param {string} body.confirmPassword - Xác nhận mật khẩu mới (must match newPassword)
   * @returns {Object} Joi validation schema
   */
  changePassword: {
    body: Joi.object().keys({
      oldPassword: Joi.string().required().messages({
        'any.required': 'Mật khẩu cũ là bắt buộc'
      }),
      newPassword: Joi.string().required().custom(password).messages({
        'any.required': 'Mật khẩu mới là bắt buộc'
      }),
      confirmPassword: Joi.string().required().custom(confirmPassword).messages({
        'any.required': 'Xác nhận mật khẩu là bắt buộc'
      })
    })
  },

  /**
   * Validation schema cho đăng xuất
   * @param {Object} body - Request body
   * @param {string} body.email - Email address (valid email format)
   * @returns {Object} Joi validation schema
   */
  logout: {
    body: Joi.object().keys({
      email: Joi.string().required().email().message({
        'string.email': 'Email không hợp lệ',
        'any.required': 'Email là bắt buộc'
      })
    })
  }
}

module.exports = authValidation
