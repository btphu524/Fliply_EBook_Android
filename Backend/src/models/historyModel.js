const db = require('../config/db')
const { ApiError } = require('../utils/index')
const { generateHistoryId } = require('../utils/idUtils')
const httpStatus = require('http-status')

const historyModel = {
  /**
   * @param {Object} historyData - History data
   * @return {Object} Created history result
   */
  create: async (historyData) => {
    try {
      if (!historyData.userId || !historyData.bookId) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ID người dùng và ID sách là bắt buộc'
        )
      }

      if (historyData.chapterId === 'null') {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ID chương không được để trống'
        )
      }

      const newHistoryId = await generateHistoryId()
      const historyId = parseInt(newHistoryId)

      const sanitizedData = {
        _id: historyId,
        userId: parseInt(historyData.userId),
        bookId: parseInt(historyData.bookId),
        chapterId: historyData.chapterId.trim(),
        lastReadAt: Date.now(),
        createdAt: Date.now(),
        updatedAt: Date.now()
      }

      const historyRef = db.getRef(`reading_history/${historyId}`)
      await historyRef.set(sanitizedData)

      return {
        historyId,
        message: 'Lịch sử đọc sách đã được tạo thành công'
      }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Tạo lịch sử đọc sách thất bại: ${error.message}`
        )
    }
  },

  /**
   * @param {number} historyId - History ID
   * @param {Object} updateData - Update data
   * @return {Object} Updated history
   */
  update: async (historyId, updateData) => {
    try {
      if (!historyId) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ID lịch sử là bắt buộc'
        )
      }

      const sanitizedUpdateData = {
        updatedAt: Date.now()
      }

      if (updateData.chapterId !== undefined) {
        sanitizedUpdateData.chapterId = updateData.chapterId.trim()
      }
      if (updateData.lastReadAt !== undefined) {
        sanitizedUpdateData.lastReadAt = updateData.lastReadAt
      }

      await db.getRef(`reading_history/${historyId}`).update(sanitizedUpdateData)
      return await historyModel.findById(historyId)
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Cập nhật lịch sử đọc sách thất bại: ${error.message}`
        )
    }
  },

  /**
   * @param {number} historyId - History ID
   * @return {Object} History data
   */
  findById: async (historyId) => {
    try {
      if (!historyId) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ID lịch sử là bắt buộc'
        )
      }

      const snapshot = await db.getRef(`reading_history/${historyId}`).once('value')
      const history = snapshot.val()
      if (!history) {
        throw new ApiError(
          httpStatus.status.NOT_FOUND,
          'Không tìm thấy lịch sử đọc sách'
        )
      }
      return { _id: historyId, ...history }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Lấy thông tin lịch sử đọc sách thất bại: ${error.message}`
        )
    }
  },

  /**
   * @param {number} userId - User ID
   * @param {number} bookId - Book ID
   * @return {Object|null} History data or null
   */
  findByUserAndBook: async (userId, bookId) => {
    try {
      if (!userId || !bookId) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ID người dùng và ID sách là bắt buộc'
        )
      }

      const snapshot = await db
        .getRef('reading_history')
        .orderByChild('userId')
        .equalTo(parseInt(userId))
        .once('value')

      const histories = snapshot.val() || {}

      for (const [historyId, history] of Object.entries(histories)) {
        if (history.bookId === parseInt(bookId)) {
          return { _id: historyId, ...history }
        }
      }

      return null
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Tìm lịch sử đọc sách thất bại: ${error.message}`
        )
    }
  },

  /**
   * @param {number} userId - User ID
   * @param {Object} options - Query options
   * @return {Object} Paginated history results
   */
  getByUserId: async (userId, options = {}) => {
    try {
      if (!userId) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ID người dùng là bắt buộc'
        )
      }

      const {
        page = 1,
        limit = 10,
        sortBy = 'lastReadAt',
        sortOrder = 'desc'
      } = options

      const snapshot = await db
        .getRef('reading_history')
        .orderByChild('userId')
        .equalTo(parseInt(userId))
        .once('value')

      const histories = snapshot.val() || {}

      let historiesArray = Object.keys(histories).map(key => ({
        _id: key,
        ...histories[key]
      }))

      historiesArray.sort((a, b) => {
        const aValue = a[sortBy] || 0
        const bValue = b[sortBy] || 0

        if (sortOrder === 'asc') {
          return aValue > bValue ? 1 : -1
        } else {
          return aValue < bValue ? 1 : -1
        }
      })

      const total = historiesArray.length
      const startIndex = (page - 1) * limit
      const endIndex = startIndex + limit
      const paginatedHistories = historiesArray.slice(startIndex, endIndex)

      return {
        histories: paginatedHistories,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total,
          totalPages: Math.ceil(total / limit)
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
   * @param {number} bookId - Book ID
   * @return {Array} History list by book
   */
  findByBook: async (bookId) => {
    try {
      if (!bookId) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ID sách là bắt buộc'
        )
      }

      const snapshot = await db
        .getRef('reading_history')
        .orderByChild('bookId')
        .equalTo(parseInt(bookId))
        .once('value')

      const histories = snapshot.val() || {}
      const historiesArray = Object.keys(histories).map(key => ({
        _id: key,
        ...histories[key]
      }))

      return historiesArray
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Tìm lịch sử đọc theo sách thất bại: ${error.message}`
        )
    }
  },

  /**
   * @param {number} historyId - History ID
   * @return {boolean} Deletion result
   */
  delete: async (historyId) => {
    try {
      if (!historyId) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ID lịch sử là bắt buộc'
        )
      }

      const history = await historyModel.findById(historyId)
      if (!history) {
        throw new ApiError(
          httpStatus.status.NOT_FOUND,
          'Không tìm thấy lịch sử đọc sách'
        )
      }

      await db.getRef(`reading_history/${historyId}`).remove()
      return true
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Xóa lịch sử đọc sách thất bại: ${error.message}`
        )
    }
  }
}

module.exports = {
  create: historyModel.create,
  update: historyModel.update,
  findById: historyModel.findById,
  findByUserAndBook: historyModel.findByUserAndBook,
  getByUserId: historyModel.getByUserId,
  findByBook: historyModel.findByBook,
  delete: historyModel.delete
}
