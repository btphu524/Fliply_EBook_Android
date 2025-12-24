const { bookModel, categoryModel } = require('../models/index')
const logger = require('../config/logger')

/**
 * Lấy danh sách sách với tìm kiếm và phân trang
 * @param {Object} data - Dữ liệu tìm kiếm
 * @param {Object} data.options - Tùy chọn tìm kiếm
 * @param {number} data.options.page - Trang hiện tại
 * @param {number} data.options.limit - Số lượng sách mỗi trang
 * @param {string} data.options.search - Từ khóa tìm kiếm
 * @param {string} data.options.category - Thể loại sách
 * @param {string} data.options.status - Trạng thái sách
 * @param {string} data.options.sortBy - Trường sắp xếp
 * @param {string} data.options.sortOrder - Thứ tự sắp xếp
 * @returns {Promise<Object>} - Danh sách sách và thông báo
 */
const getBooksList = async (data) => {
  const { options = {} } = data || {}
  try {
    const result = await bookModel.search(options)
    return {
      success: true,
      data: result,
      message: 'Lấy danh sách sách thành công'
    }
  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  }
}

/**
 * Lấy thông tin sách theo ID
 * @param {Object} data - Dữ liệu yêu cầu
 * @param {string} data.id - ID của sách
 * @returns {Promise<Object>} - Thông tin sách và thông báo
 */
const getBookById = async (data) => {
  const { id } = data
  try {
    const book = await bookModel.getById(id)
    return {
      success: true,
      data: { book },
      message: 'Lấy sách thành công'
    }
  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  }
}


/**
 * Lấy danh sách sách mới nhất
 * @param {Object} data - Dữ liệu yêu cầu
 * @param {number} data.limit - Số lượng sách tối đa
 * @returns {Promise<Object>} - Danh sách sách mới nhất và thông báo
 */
const getLatestBooks = async (data) => {
  const { limit = 10 } = data || {}
  try {
    const books = await bookModel.getLatest(limit)
    return {
      success: true,
      data: { books },
      message: 'Lấy sách mới nhất thành công'
    }
  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  }
}

/**
 * Lấy ID sách lớn nhất hiện tại
 * @returns {Promise<Object>} - ID sách lớn nhất và thông báo
 */
const getCurrentMaxBookId = async () => {
  try {
    const maxId = await bookModel.getMaxId()
    return {
      success: true,
      data: { currentMaxId: maxId },
      message: 'Lấy ID sách lớn nhất thành công'
    }
  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  }
}

/**
 * Lấy chi tiết danh sách sách yêu thích
 * @param {Object} data - Dữ liệu yêu cầu
 * @param {Array} data.bookIds - Mảng ID các sách yêu thích
 * @returns {Promise<Object>} - Danh sách sách yêu thích và thông báo
 */
const getFavoriteBooksDetails = async (data) => {
  const { bookIds } = data
  try {
    if (!Array.isArray(bookIds) || bookIds.length === 0) {
      return {
        success: true,
        data: { books: [] }
      }
    }

    const books = []
    for (const bookId of bookIds) {
      try {
        const book = await bookModel.getById(bookId)
        if (book) {
          books.push(book)
        }
      } catch (error) {
        logger.warn(`Book with ID ${bookId} not found`)
      }
    }

    return {
      success: true,
      data: { books },
      message: 'Lấy danh sách sách yêu thích thành công'
    }
  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  }
}

/**
 * Tìm kiếm nhanh theo một input duy nhất
 * @param {Object} data
 * @param {string} data.input
 * @param {number} [data.page]
 * @param {number} [data.limit]
 */
const quickSearch = async (data) => {
  const { input = '', page = 1, limit = 20 } = data || {}
  try {
    const normalize = (str = '') => String(str).toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '')
    const needle = normalize(input.trim())
    if (!needle) {
      return { success: true, data: { books: [] } }
    }

    const categoriesObj = await categoryModel.findAll()
    const categoryIdMatches = new Set()
    if (categoriesObj) {
      for (const [catId, cat] of Object.entries(categoriesObj)) {
        if (normalize(cat?.name || '').includes(needle)) {
          categoryIdMatches.add(String(catId))
        }
      }
    }

    const allBooks = await bookModel.getAll()
    const filtered = allBooks.filter(book => {
      const nTitle = normalize(book.title)
      const nAuthor = normalize(book.author)
      const nDesc = normalize(book.description)
      const nKeywords = Array.isArray(book.keywords) ? normalize(book.keywords.join(' ')) : normalize(book.keywords)
      const catMatch = categoryIdMatches.has(String(book.category))
      return (
        nTitle.includes(needle) ||
        nAuthor.includes(needle) ||
        nDesc.includes(needle) ||
        nKeywords.includes(needle) ||
        catMatch
      )
    })

    const total = filtered.length
    const p = parseInt(page) || 1
    const l = Math.min(50, parseInt(limit) || 20)
    const startIndex = (p - 1) * l
    const endIndex = startIndex + l
    const books = filtered.slice(startIndex, endIndex)

    return { success: true, data: { books, pagination: { page: p, limit: l, total, totalPages: Math.ceil(total / l) } } }
  } catch (error) {
    return { success: false, message: error.message }
  }
}

module.exports = {
  getBooksList,
  quickSearch,
  getBookById,
  getLatestBooks,
  getCurrentMaxBookId,
  getFavoriteBooksDetails
}
