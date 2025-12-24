const db = require('../config/db')

const bookModel = {
  /**
   * Lấy tất cả sách (chỉ sách active, loại bỏ sách đã bị xóa mềm)
   * @returns {Promise<Array>} Danh sách sách active
   */
  getAll: async () => {
    try {
      const snapshot = await db.getRef('books').once('value')
      const books = snapshot.val() || {}

      const booksArray = Object.keys(books)
        .map(key => ({
          _id: key,
          ...books[key]
        }))
        .filter(book => book.isActive !== false)

      return booksArray
    } catch (error) {
      throw new Error(`Lỗi khi lấy danh sách sách: ${error.message}`)
    }
  },

  /**
   * Lấy sách theo ID (chỉ sách active)
   * @param {string|number} id - ID của sách
   * @returns {Promise<Object>} Thông tin sách active
   * @throws {Error} Nếu sách không tồn tại hoặc đã bị xóa mềm
   */
  getById: async (id) => {
    try {
      const snapshot = await db.getRef(`books/${id}`).once('value')
      const book = snapshot.val()

      if (!book) {
        throw new Error('Không tìm thấy sách')
      }

      if (book.isActive === false) {
        throw new Error('Sách đã bị xóa')
      }

      return { _id: id, ...book }
    } catch (error) {
      throw new Error(`Lỗi khi lấy sách: ${error.message}`)
    }
  },

  /**
   * Tạo sách mới
   * @param {Object} bookData
   * @returns {Promise<Object>} Sách đã tạo
   */
  create: async (bookData) => {
    try {
      const snapshot = await db.getRef('books').once('value')
      const books = snapshot.val() || {}
      const maxId = Math.max(0, ...Object.keys(books).map(Number).filter(id => !isNaN(id)))
      const newId = maxId + 1

      const today = new Date()
      const todayString = today.toISOString().split('T')[0]

      const newBook = {
        _id: newId,
        title: bookData.title || '',
        author: bookData.author || '',
        category: bookData.category || null,
        description: bookData.description || '',
        release_date: bookData.release_date || todayString,
        cover_url: bookData.cover_url || '',
        txt_url: bookData.txt_url || '',
        book_url: bookData.book_url || '',
        epub_url: bookData.epub_url || '',
        keywords: bookData.keywords || [],
        status: bookData.status || 'active',
        avgRating: bookData.avgRating || 0,
        numberOfReviews: bookData.numberOfReviews || 0,
        createdAt: today.toISOString(),
        updatedAt: today.toISOString()
      }

      await db.getRef(`books/${newId}`).set(newBook)
      return newBook
    } catch (error) {
      throw new Error(`Lỗi khi tạo sách: ${error.message}`)
    }
  },

  /**
   * Cập nhật thông tin sách
   * @param {string|number} id - ID của sách
   * @param {Object} updateData - Dữ liệu cần cập nhật
   * @returns {Promise<Object>} Sách đã được cập nhật
   * @throws {Error} Nếu sách không tồn tại
   */
  update: async (id, updateData) => {
    try {
      const existingBook = await bookModel.getById(id)
      if (!existingBook) {
        throw new Error('Không tìm thấy sách')
      }

      const updatedData = {
        ...updateData,
        updatedAt: new Date().toISOString()
      }

      await db.getRef(`books/${id}`).update(updatedData)
      return await bookModel.getById(id)
    } catch (error) {
      throw new Error(`Lỗi khi cập nhật sách: ${error.message}`)
    }
  },

  /**
   * Xóa mềm sách (soft delete)
   * Đánh dấu isActive = false thay vì xóa hẳn khỏi database
   * @param {string|number} id - ID của sách
   * @returns {Promise<boolean>} True nếu xóa thành công
   * @throws {Error} Nếu sách không tồn tại
   */
  delete: async (id) => {
    try {
      const existingBook = await bookModel.getById(id)
      if (!existingBook) {
        throw new Error('Không tìm thấy sách')
      }

      await db.getRef(`books/${id}`).update({
        isActive: false,
        deletedAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      })
      return true
    } catch (error) {
      throw new Error(`Lỗi khi xóa sách: ${error.message}`)
    }
  },

  /**
   * Xóa vĩnh viễn sách khỏi database (hard delete)
   * Chỉ dành cho admin, không thể khôi phục
   * @param {string|number} id - ID của sách
   * @returns {Promise<boolean>} True nếu xóa thành công
   * @throws {Error} Nếu sách không tồn tại
   */
  hardDelete: async (id) => {
    try {
      const existingBook = await bookModel.getById(id)
      if (!existingBook) {
        throw new Error('Không tìm thấy sách')
      }

      await db.getRef(`books/${id}`).remove()
      return true
    } catch (error) {
      throw new Error(`Lỗi khi xóa sách vĩnh viễn: ${error.message}`)
    }
  },

  /**
   * Khôi phục sách đã bị xóa mềm
   * @param {string|number} id - ID của sách
   * @returns {Promise<boolean>} True nếu khôi phục thành công
   * @throws {Error} Nếu sách không tồn tại hoặc chưa bị xóa mềm
   */
  restore: async (id) => {
    try {
      const bookRef = db.getRef(`books/${id}`)
      const snapshot = await bookRef.once('value')
      const book = snapshot.val()

      if (!book) {
        throw new Error('Không tìm thấy sách')
      }

      if (book.isActive !== false) {
        throw new Error('Sách chưa bị xóa mềm')
      }

      await bookRef.update({
        isActive: true,
        restoredAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      })
      return true
    } catch (error) {
      throw new Error(`Lỗi khi khôi phục sách: ${error.message}`)
    }
  },

  /**
   * Lấy danh sách sách mới nhất (chỉ sách active)
   * @param {number} limit - Số lượng sách cần lấy (mặc định: 10)
   * @returns {Promise<Array>} Danh sách sách mới nhất
   * @throws {Error} Nếu có lỗi khi lấy dữ liệu
   */
  getLatest: async (limit = 10) => {
    try {
      const allBooks = await bookModel.getAll()
      allBooks.sort((a, b) => {
        const dateA = new Date(a.createdAt || '1970-01-01T00:00:00.000Z')
        const dateB = new Date(b.createdAt || '1970-01-01T00:00:00.000Z')
        return dateB - dateA
      })

      return allBooks.slice(0, limit)
    } catch (error) {
      throw new Error(`Lỗi khi lấy sách mới nhất: ${error.message}`)
    }
  },

  /**
   * Lấy ID lớn nhất của sách
   * @returns {Promise<number>} ID lớn nhất
   * @throws {Error} Nếu có lỗi khi lấy dữ liệu
   */
  getMaxId: async () => {
    try {
      const allBooks = await bookModel.getAll()
      if (allBooks.length === 0) return 0

      const maxId = Math.max(...allBooks.map(book => parseInt(book._id) || 0))
      return maxId
    } catch (error) {
      throw new Error(`Lỗi khi lấy ID lớn nhất: ${error.message}`)
    }
  },

  /**
   * Tìm kiếm sách với nhiều tiêu chí (chỉ sách active)
   * @param {Object} options - Các tùy chọn tìm kiếm
   * @param {number} options.page - Số trang (mặc định: 1)
   * @param {number} options.limit - Số sách mỗi trang (mặc định: 10)
   * @param {string} options.search - Từ khóa tìm kiếm chung
   * @param {string} options.q - Từ khóa tìm kiếm (alias của search)
   * @param {string} options.title - Tìm theo tiêu đề
   * @param {string} options.author - Tìm theo tác giả
   * @param {string} options.keyword - Tìm theo từ khóa
   * @param {string} options.category - Lọc theo thể loại
   * @param {string} options.status - Lọc theo trạng thái (mặc định: 'active')
   * @param {string} options.sortBy - Sắp xếp theo trường (mặc định: 'createdAt')
   * @param {string} options.sortOrder - Thứ tự sắp xếp 'asc'/'desc' (mặc định: 'desc')
   * @returns {Promise<Object>} Kết quả tìm kiếm với phân trang
   * @throws {Error} Nếu có lỗi khi tìm kiếm
   */
  search: async (options = {}) => {
    try {
      const {
        page = 1,
        limit = 10,
        search = '', // backward-compat
        q = '',
        title = '',
        author = '',
        keyword = '',
        category = '',
        status = 'active',
        sortBy = 'createdAt',
        sortOrder = 'desc'
      } = options

      let allBooks = await bookModel.getAll()

      const normalize = (str = '') =>
        String(str)
          .toLowerCase()
          .normalize('NFD')
          .replace(/[\u0300-\u036f]/g, '')

      const normSearch = normalize(search)
      const normQ = normalize(q)
      const normTitle = normalize(title)
      const normAuthor = normalize(author)
      const normKeyword = normalize(keyword)

      if (normQ || normSearch) {
        const needle = normQ || normSearch
        allBooks = allBooks.filter(book => {
          const nTitle = normalize(book.title)
          const nAuthor = normalize(book.author)
          const nDesc = normalize(book.description)
          const nKeywords = Array.isArray(book.keywords)
            ? normalize(book.keywords.join(' '))
            : normalize(book.keywords)

          return (
            nTitle.includes(needle) ||
            nAuthor.includes(needle) ||
            nDesc.includes(needle) ||
            nKeywords.includes(needle)
          )
        })
      }

      if (normTitle) {
        allBooks = allBooks.filter(book => normalize(book.title).includes(normTitle))
      }

      if (normAuthor) {
        allBooks = allBooks.filter(book => normalize(book.author).includes(normAuthor))
      }

      if (normKeyword) {
        allBooks = allBooks.filter(book => {
          if (!book.keywords) return false
          const list = Array.isArray(book.keywords) ? book.keywords : [book.keywords]
          return list.some(k => normalize(k).includes(normKeyword))
        })
      }

      if (category) {
        allBooks = allBooks.filter(book => book.category == category)
      }

      if (status) {
        allBooks = allBooks.filter(book => book.status === status)
      }

      allBooks.sort((a, b) => {
        const aValue = a[sortBy] || ''
        const bValue = b[sortBy] || ''

        if (sortOrder === 'asc') {
          return aValue > bValue ? 1 : -1
        } else {
          return aValue < bValue ? 1 : -1
        }
      })

      const total = allBooks.length
      const startIndex = (page - 1) * limit
      const endIndex = startIndex + limit
      const paginatedBooks = allBooks.slice(startIndex, endIndex)

      return {
        books: paginatedBooks,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total,
          totalPages: Math.ceil(total / limit)
        }
      }
    } catch (error) {
      throw new Error(`Lỗi khi tìm kiếm sách: ${error.message}`)
    }
  }
}

module.exports = bookModel
