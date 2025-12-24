const Joi = require('joi')

/**
 * Validation cho lấy danh sách sách
 * @param {Object} query - Query parameters
 * @param {number} [query.page=1] - Page number (positive integer)
 * @param {number} [query.limit=10] - Items per page (1-100)
 * @param {string} [query.search] - Search term
 * @param {string} [query.q] - Search term (alias)
 * @param {string} [query.title] - Filter by title
 * @param {string} [query.author] - Filter by author
 * @param {string} [query.keyword] - Filter by keyword
 * @param {number} [query.category] - Filter by category ID
 * @param {string} [query.status='active'] - Filter by status
 * @param {string} [query.sortBy='createdAt'] - Sort field ('title', 'author', 'createdAt', 'updatedAt')
 * @param {string} [query.sortOrder='desc'] - Sort order ('asc', 'desc')
 * @return {Object} Joi validation schema
 */
const getList = {
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
    search: Joi.string().trim().allow('').messages({
      'string.base': 'Từ khóa tìm kiếm phải là chuỗi'
    }),
    q: Joi.string().trim().allow('').messages({
      'string.base': 'Từ khóa tìm kiếm phải là chuỗi'
    }),
    title: Joi.string().trim().allow('').messages({
      'string.base': 'Tiêu đề phải là chuỗi'
    }),
    author: Joi.string().trim().allow('').messages({
      'string.base': 'Tác giả phải là chuỗi'
    }),
    keyword: Joi.string().trim().allow('').messages({
      'string.base': 'Từ khóa phải là chuỗi'
    }),
    category: Joi.number().integer().positive().messages({
      'number.base': 'Thể loại phải là số nguyên',
      'number.integer': 'Thể loại phải là số nguyên',
      'number.positive': 'Thể loại phải là số dương'
    }),
    status: Joi.string().valid('active', 'inactive').default('active').messages({
      'any.only': 'Trạng thái phải là active hoặc inactive'
    }),
    sortBy: Joi.string().valid('title', 'author', 'createdAt', 'updatedAt').default('createdAt').messages({
      'any.only': 'Sắp xếp theo phải là title, author, createdAt hoặc updatedAt'
    }),
    sortOrder: Joi.string().valid('asc', 'desc').default('desc').messages({
      'any.only': 'Thứ tự sắp xếp phải là asc hoặc desc'
    })
  })
}

/**
 * Validation cho lấy sách mới nhất
 * @param {Object} query - Query parameters
 * @param {number} [query.limit=10] - Number of books to return (1-50)
 * @return {Object} Joi validation schema
 */
const getLatest = {
  query: Joi.object().keys({
    limit: Joi.number().integer().min(1).max(50).default(10).messages({
      'number.base': 'Số lượng sách phải là số nguyên',
      'number.integer': 'Số lượng sách phải là số nguyên',
      'number.min': 'Số lượng sách phải lớn hơn 0',
      'number.max': 'Số lượng sách không được vượt quá 50'
    })
  })
}

/**
 * Validation cho lấy sách theo ID
 * @param {Object} params - Route parameters
 * @param {string} params.id - Book ID (positive integer)
 * @return {Object} Joi validation schema
 */
const getById = {
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
 * Validation cho tìm kiếm sách
 * @param {Object} query - Query parameters
 * @param {string} [query.input] - Search input
 * @param {string} [query.q] - Search query (alias)
 * @param {string} [query.search] - Search term (alias)
 * @param {number} [query.page=1] - Page number
 * @param {number} [query.limit=10] - Items per page
 * @return {Object} Joi validation schema
 */
const quickSearch = {
  query: Joi.object().keys({
    input: Joi.string().trim().allow('').messages({
      'string.base': 'Từ khóa tìm kiếm phải là chuỗi'
    }),
    q: Joi.string().trim().allow('').messages({
      'string.base': 'Từ khóa tìm kiếm phải là chuỗi'
    }),
    search: Joi.string().trim().allow('').messages({
      'string.base': 'Từ khóa tìm kiếm phải là chuỗi'
    }),
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
    })
  })
}

module.exports = {
  getList,
  getLatest,
  getById,
  quickSearch
}
