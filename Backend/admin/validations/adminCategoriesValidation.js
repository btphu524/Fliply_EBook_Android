const Joi = require('joi')
/**
 * Tạo category mới
 */
const createCategory = {
  body: Joi.object().keys({
    name: Joi.string().required().trim().min(1).max(100).messages({
      'string.empty': 'Tên thể loại không được để trống',
      'string.min': 'Tên thể loại phải có ít nhất 1 ký tự',
      'string.max': 'Tên thể loại không được vượt quá 100 ký tự',
      'any.required': 'Tên thể loại là bắt buộc'
    }),
    image_url: Joi.string().uri().allow('').messages({
      'string.uri': 'URL ảnh thể loại không hợp lệ'
    }),
    status: Joi.string().valid('active', 'inactive').default('active').messages({
      'any.only': 'Trạng thái phải là active hoặc inactive'
    })
  }).unknown(false).messages({
    'object.unknown': 'Trường {#label} không được phép. Vui lòng không gửi các trường hệ thống như _id, createdAt, updatedAt'
  })
}

/**
 * Cập nhật category
 */
const updateCategory = {
  params: Joi.object().keys({
    categoryId: Joi.number().integer().positive().required().messages({
      'number.base': 'ID thể loại phải là số nguyên',
      'number.integer': 'ID thể loại phải là số nguyên',
      'number.positive': 'ID thể loại phải là số dương',
      'any.required': 'ID thể loại là bắt buộc'
    })
  }),
  body: Joi.object().keys({
    name: Joi.string().trim().min(1).max(100).messages({
      'string.empty': 'Tên thể loại không được để trống',
      'string.min': 'Tên thể loại phải có ít nhất 1 ký tự',
      'string.max': 'Tên thể loại không được vượt quá 100 ký tự'
    }),
    image_url: Joi.string().uri().allow('').messages({
      'string.uri': 'URL ảnh thể loại không hợp lệ'
    }),
    status: Joi.string().valid('active', 'inactive').messages({
      'any.only': 'Trạng thái phải là active hoặc inactive'
    })
  }).min(1).unknown(false).messages({
    'object.min': 'Phải cung cấp ít nhất một trường để cập nhật',
    'object.unknown': 'Trường {#label} không được phép. Vui lòng không gửi các trường hệ thống như _id, createdAt, updatedAt'
  })
}

/**
 * Xóa category
 */
const deleteCategory = {
  params: Joi.object().keys({
    categoryId: Joi.number().integer().positive().required().messages({
      'number.base': 'ID thể loại phải là số nguyên',
      'number.integer': 'ID thể loại phải là số nguyên',
      'number.positive': 'ID thể loại phải là số dương',
      'any.required': 'ID thể loại là bắt buộc'
    })
  })
}

/**
 * Xóa vĩnh viễn category
 */
const hardDeleteCategory = {
  params: Joi.object().keys({
    categoryId: Joi.number().integer().positive().required().messages({
      'number.base': 'ID thể loại phải là số nguyên',
      'number.integer': 'ID thể loại phải là số nguyên',
      'number.positive': 'ID thể loại phải là số dương',
      'any.required': 'ID thể loại là bắt buộc'
    })
  })
}

/**
 * Khôi phục category
 */
const restoreCategory = {
  params: Joi.object().keys({
    categoryId: Joi.number().integer().positive().required().messages({
      'number.base': 'ID thể loại phải là số nguyên',
      'number.integer': 'ID thể loại phải là số nguyên',
      'number.positive': 'ID thể loại phải là số dương',
      'any.required': 'ID thể loại là bắt buộc'
    })
  })
}

/**
 * Lấy danh sách categories đã xóa
 */
const getDeletedCategories = {
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
    sortBy: Joi.string().valid('createdAt', 'updatedAt', 'deletedAt', 'name').default('deletedAt').messages({
      'any.only': 'Sắp xếp theo phải là createdAt, updatedAt, deletedAt hoặc name'
    }),
    sortOrder: Joi.string().valid('asc', 'desc').default('desc').messages({
      'any.only': 'Thứ tự sắp xếp phải là asc hoặc desc'
    })
  })
}

module.exports = {
  createCategory,
  updateCategory,
  deleteCategory,
  hardDeleteCategory,
  restoreCategory,
  getDeletedCategories
}
