const Joi = require('joi')

/**
 * Tạo sách mới
 */
const createBook = {
  body: Joi.object().keys({
    title: Joi.string().required().trim().min(1).max(255).messages({
      'string.empty': 'Tiêu đề sách không được để trống',
      'string.min': 'Tiêu đề sách phải có ít nhất 1 ký tự',
      'string.max': 'Tiêu đề sách không được vượt quá 255 ký tự',
      'any.required': 'Tiêu đề sách là bắt buộc'
    }),
    author: Joi.string().required().trim().min(1).max(255).messages({
      'string.empty': 'Tác giả không được để trống',
      'string.min': 'Tác giả phải có ít nhất 1 ký tự',
      'string.max': 'Tác giả không được vượt quá 255 ký tự',
      'any.required': 'Tác giả là bắt buộc'
    }),
    category: Joi.number().integer().positive().allow(null).messages({
      'number.base': 'Thể loại phải là số nguyên',
      'number.integer': 'Thể loại phải là số nguyên',
      'number.positive': 'Thể loại phải là số dương'
    }),
    description: Joi.string().allow('').max(2000).messages({
      'string.max': 'Mô tả không được vượt quá 2000 ký tự'
    }),
    release_date: Joi.date().iso().allow('').messages({
      'date.format': 'Ngày phát hành phải có định dạng ISO'
    }),
    cover_url: Joi.string().uri().allow('').messages({
      'string.uri': 'URL ảnh bìa không hợp lệ'
    }),
    txt_url: Joi.string().uri().allow('').messages({
      'string.uri': 'URL file txt không hợp lệ'
    }),
    book_url: Joi.string().uri().allow('').messages({
      'string.uri': 'URL sách không hợp lệ'
    }),
    epub_url: Joi.string().uri().allow('').messages({
      'string.uri': 'URL file epub không hợp lệ'
    }),
    keywords: Joi.array().items(Joi.string().trim()).default([]).messages({
      'array.base': 'Từ khóa phải là mảng'
    }),
    status: Joi.string().valid('active', 'inactive', 'draft').default('active').messages({
      'any.only': 'Trạng thái phải là active, inactive hoặc draft'
    })
  }).unknown(false).messages({
    'object.unknown': 'Trường {#label} không được phép. Vui lòng không gửi các trường hệ thống như _id, createdAt, updatedAt'
  })
}

/**
 * Cập nhật sách
 */
const updateBook = {
  params: Joi.object().keys({
    id: Joi.number().integer().positive().required().messages({
      'number.base': 'ID sách phải là số nguyên',
      'number.integer': 'ID sách phải là số nguyên',
      'number.positive': 'ID sách phải là số dương',
      'any.required': 'ID sách là bắt buộc'
    })
  }),
  body: Joi.object().keys({
    title: Joi.string().trim().min(1).max(255).messages({
      'string.empty': 'Tiêu đề sách không được để trống',
      'string.min': 'Tiêu đề sách phải có ít nhất 1 ký tự',
      'string.max': 'Tiêu đề sách không được vượt quá 255 ký tự'
    }),
    author: Joi.string().trim().min(1).max(255).messages({
      'string.empty': 'Tác giả không được để trống',
      'string.min': 'Tác giả phải có ít nhất 1 ký tự',
      'string.max': 'Tác giả không được vượt quá 255 ký tự'
    }),
    category: Joi.number().integer().positive().allow(null).messages({
      'number.base': 'Thể loại phải là số nguyên',
      'number.integer': 'Thể loại phải là số nguyên',
      'number.positive': 'Thể loại phải là số dương'
    }),
    description: Joi.string().allow('').max(2000).messages({
      'string.max': 'Mô tả không được vượt quá 2000 ký tự'
    }),
    release_date: Joi.date().iso().allow('').messages({
      'date.format': 'Ngày phát hành phải có định dạng ISO'
    }),
    cover_url: Joi.string().uri().allow('').messages({
      'string.uri': 'URL ảnh bìa không hợp lệ'
    }),
    txt_url: Joi.string().uri().allow('').messages({
      'string.uri': 'URL file txt không hợp lệ'
    }),
    book_url: Joi.string().uri().allow('').messages({
      'string.uri': 'URL sách không hợp lệ'
    }),
    epub_url: Joi.string().uri().allow('').messages({
      'string.uri': 'URL file epub không hợp lệ'
    }),
    keywords: Joi.array().items(Joi.string().trim()).messages({
      'array.base': 'Từ khóa phải là mảng'
    }),
    status: Joi.string().valid('active', 'inactive', 'draft').messages({
      'any.only': 'Trạng thái phải là active, inactive hoặc draft'
    })
  }).min(1).unknown(false).messages({
    'object.min': 'Phải cung cấp ít nhất một trường để cập nhật',
    'object.unknown': 'Trường {#label} không được phép. Vui lòng không gửi các trường hệ thống như _id, createdAt, updatedAt'
  })
}

/**
 * Xóa sách
 */
const deleteBook = {
  params: Joi.object().keys({
    id: Joi.number().integer().positive().required().messages({
      'number.base': 'ID sách phải là số nguyên',
      'number.integer': 'ID sách phải là số nguyên',
      'number.positive': 'ID sách phải là số dương',
      'any.required': 'ID sách là bắt buộc'
    })
  })
}

/**
 * Xóa vĩnh viễn sách
 */
const hardDeleteBook = {
  params: Joi.object().keys({
    id: Joi.number().integer().positive().required().messages({
      'number.base': 'ID sách phải là số nguyên',
      'number.integer': 'ID sách phải là số nguyên',
      'number.positive': 'ID sách phải là số dương',
      'any.required': 'ID sách là bắt buộc'
    })
  })
}

/**
 * Khôi phục sách
 */
const restoreBook = {
  params: Joi.object().keys({
    id: Joi.number().integer().positive().required().messages({
      'number.base': 'ID sách phải là số nguyên',
      'number.integer': 'ID sách phải là số nguyên',
      'number.positive': 'ID sách phải là số dương',
      'any.required': 'ID sách là bắt buộc'
    })
  })
}

/**
 * Lấy danh sách sách đã xóa
 */
const getDeletedBooks = {
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
    sortBy: Joi.string().valid('createdAt', 'updatedAt', 'deletedAt', 'title', 'author').default('deletedAt').messages({
      'any.only': 'Sắp xếp theo phải là createdAt, updatedAt, deletedAt, title hoặc author'
    }),
    sortOrder: Joi.string().valid('asc', 'desc').default('desc').messages({
      'any.only': 'Thứ tự sắp xếp phải là asc hoặc desc'
    })
  })
}

module.exports = {
  createBook,
  updateBook,
  deleteBook,
  hardDeleteBook,
  restoreBook,
  getDeletedBooks
}
