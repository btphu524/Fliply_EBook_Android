const historyModel = require('../models/historyModel')
const bookModel = require('../models/bookModel')
const userModel = require('../models/userModel')
const { ApiError } = require('../utils/index')
const httpStatus = require('http-status')
const epubService = require('./epubService')

const historyService = {
  /**
   * Lưu bookmark cho người dùng
   * @param {Object} bookmarkData - Dữ liệu bookmark
   * @returns {Promise<Object>} - Kết quả lưu bookmark
   * @throws {ApiError} - Nếu lưu bookmark thất bại
   */
  saveBookmark: async (bookmarkData) => {
    try {
      const { userId, bookId, chapterId } = bookmarkData

      await userModel.findById(userId)

      await bookModel.getById(bookId)

      if (chapterId === 'null') {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ChapterId là bắt buộc'
        )
      }

      await epubService.getEpubChapterRaw({ url: bookModel.epub_url, chapterId: chapterId })

      const existingHistory = await historyModel.findByUserAndBook(userId, bookId)

      const updateData = {
        chapterId,
        lastReadAt: Date.now()
      }


      if (existingHistory) {
        const updatedHistory = await historyModel.update(existingHistory._id, updateData)

        const { page, ...historyWithoutPage } = updatedHistory
        return {
          success: true,
          message: 'Bookmark đã được cập nhật thành công',
          data: historyWithoutPage
        }
      } else {
        const newHistory = await historyModel.create({
          userId,
          bookId,
          ...updateData
        })

        const createdHistory = await historyModel.findById(newHistory.historyId)
        const { page, ...historyWithoutPage } = createdHistory
        return {
          success: true,
          message: 'Bookmark đã được lưu thành công',
          data: historyWithoutPage
        }
      }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Lưu bookmark thất bại: ${error.message}`
        )
    }
  },

  /**
   * Lấy lịch sử đọc của người dùng
   * @param {number} userId - ID người dùng
   * @param {Object} options - Tùy chọn phân trang
   * @returns {Promise<Object>} - Lịch sử đọc và thông tin phân trang
   * @throws {ApiError} - Nếu lấy lịch sử thất bại
   */
  getReadingHistory: async (userId, options = {}) => {
    try {
      await userModel.findById(userId)

      const historyResult = await historyModel.getByUserId(userId, options)
      const { histories, pagination } = historyResult

      const historiesWithBooks = await Promise.all(
        histories.map(async (history) => {
          try {
            const book = await bookModel.getById(history.bookId)
            return {
              ...history,
              book: {
                _id: book._id,
                title: book.title,
                author: book.author,
                cover_url: book.cover_url,
                category: book.category
              }
            }
          } catch (error) {
            return {
              ...history,
              book: null
            }
          }
        })
      )

      return {
        success: true,
        message: 'Lấy lịch sử đọc sách thành công',
        data: {
          histories: historiesWithBooks,
          pagination
        }
      }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Lấy lịch sử đọc sách thất bại: ${error.message}`
        )
    }
  },

  /**
   * Lấy bookmark của người dùng cho một cuốn sách
   * @param {number} userId - ID người dùng
   * @param {number} bookId - ID sách
   * @returns {Promise<Object>} - Thông tin bookmark
   * @throws {ApiError} - Nếu lấy bookmark thất bại
   */
  getBookmark: async (userId, bookId) => {
    try {
      await userModel.findById(userId)

      const book = await bookModel.getById(bookId)

      const history = await historyModel.findByUserAndBook(userId, bookId)

      if (!history) {
        return {
          success: true,
          message: 'Chưa có bookmark cho cuốn sách này',
          data: {
            bookId: parseInt(bookId),
            page: 1,
            lastReadAt: null,
            book: {
              _id: book._id,
              title: book.title,
              author: book.author,
              cover_url: book.cover_url
            }
          }
        }
      }

      return {
        success: true,
        message: 'Lấy bookmark thành công',
        data: {
          ...history,
          book: {
            _id: book._id,
            title: book.title,
            author: book.author,
            cover_url: book.cover_url
          }
        }
      }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Lấy bookmark thất bại: ${error.message}`
        )
    }
  },

  /**
   * Xóa bookmark của người dùng
   * @param {number} userId - ID người dùng
   * @param {number} bookId - ID sách
   * @returns {Promise<Object>} - Kết quả xóa bookmark
   * @throws {ApiError} - Nếu xóa bookmark thất bại
   */
  deleteBookmark: async (userId, bookId) => {
    try {
      await userModel.findById(userId)

      const history = await historyModel.findByUserAndBook(userId, bookId)

      if (!history) {
        throw new ApiError(
          httpStatus.status.NOT_FOUND,
          'Không tìm thấy bookmark cho cuốn sách này'
        )
      }

      await historyModel.delete(history._id)

      return {
        success: true,
        message: 'Xóa bookmark thành công'
      }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Xóa bookmark thất bại: ${error.message}`
        )
    }
  },

  /**
   * Lấy lịch sử đọc theo người dùng
   * @param {number} userId - ID người dùng
   * @returns {Promise<Object>} - Lịch sử đọc của người dùng
   * @throws {ApiError} - Nếu lấy lịch sử thất bại
   */
  getHistoryByUser: async (userId) => {
    try {
      await userModel.findById(userId)

      const historyResult = await historyModel.getByUserId(userId)
      const histories = historyResult.histories

      return {
        success: true,
        message: `Lấy lịch sử đọc của user ${userId} thành công`,
        data: histories,
        count: histories.length
      }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Lấy lịch sử đọc theo user thất bại: ${error.message}`
        )
    }
  },

  /**
   * Lấy lịch sử đọc theo sách
   * @param {number} bookId - ID sách
   * @returns {Promise<Object>} - Lịch sử đọc của sách
   * @throws {ApiError} - Nếu lấy lịch sử thất bại
   */
  getHistoryByBook: async (bookId) => {
    try {
      await bookModel.getById(bookId)

      const histories = await historyModel.findByBook(bookId)

      return {
        success: true,
        message: `Lấy lịch sử đọc của book ${bookId} thành công`,
        data: histories,
        count: histories.length
      }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Lấy lịch sử đọc theo book thất bại: ${error.message}`
        )
    }
  }
}

module.exports = {
  saveBookmark: historyService.saveBookmark,
  getReadingHistory: historyService.getReadingHistory,
  getBookmark: historyService.getBookmark,
  deleteBookmark: historyService.deleteBookmark,
  getHistoryByUser: historyService.getHistoryByUser,
  getHistoryByBook: historyService.getHistoryByBook
}
