const db = require('../config/db')
const { ApiError } = require('../utils/index')
const httpStatus = require('http-status')
const { generateFeedbackId } = require('../utils/idUtils')

const feedbackModel = {
  /**
   * Tạo feedback mới
   * @param {Object} feedbackData - Dữ liệu feedback
   * @returns {Promise<Object>} - Kết quả tạo feedback
   * @throws {ApiError} - Nếu tạo feedback thất bại
   */
  create: async (feedbackData) => {
    try {
      if (!feedbackData.userId || !feedbackData.comment) {
        throw new ApiError(
          httpStatus.BAD_REQUEST,
          'ID người dùng và comment là bắt buộc'
        )
      }

      const newFeedbackId = await generateFeedbackId()
      const feedbackId = parseInt(newFeedbackId)

      const sanitizedData = {
        _id: feedbackId,
        userId: parseInt(feedbackData.userId),
        fullName: feedbackData.fullName.trim(),
        phoneNumber: feedbackData.phoneNumber ? parseInt(feedbackData.phoneNumber) : null,
        email: feedbackData.email ? feedbackData.email.trim().toLowerCase() : null,
        comment: feedbackData.comment.trim(),
        status: 'pending',
        createdAt: Date.now(),
        updatedAt: Date.now()
      }

      const feedbackRef = db.getRef(`feedbacks/${feedbackId}`)
      await feedbackRef.set(sanitizedData)

      return {
        feedbackId,
        message: 'Phản hồi đã được tạo thành công'
      }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.INTERNAL_SERVER_ERROR,
          `Tạo phản hồi thất bại: ${error.message}`
        )
    }
  },

  /**
   * Tìm feedback theo ID
   * @param {number} feedbackId - ID feedback
   * @returns {Promise<Object>} - Thông tin feedback
   * @throws {ApiError} - Nếu không tìm thấy feedback
   */
  findById: async (feedbackId) => {
    try {
      if (!feedbackId) {
        throw new ApiError(
          httpStatus.BAD_REQUEST,
          'ID phản hồi là bắt buộc'
        )
      }

      const snapshot = await db.getRef(`feedbacks/${feedbackId}`).once('value')
      const feedback = snapshot.val()
      if (!feedback) {
        throw new ApiError(
          httpStatus.NOT_FOUND,
          'Không tìm thấy phản hồi'
        )
      }
      return { _id: feedbackId, ...feedback }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.INTERNAL_SERVER_ERROR,
          `Tìm phản hồi theo ID thất bại: ${error.message}`
        )
    }
  },

  /**
   * Tìm feedback theo user ID
   * @param {number} userId - ID người dùng
   * @returns {Promise<Array>} - Danh sách feedback
   * @throws {ApiError} - Nếu tìm kiếm thất bại
   */
  findByUserId: async (userId) => {
    try {
      if (!userId) {
        throw new ApiError(
          httpStatus.BAD_REQUEST,
          'ID người dùng là bắt buộc'
        )
      }

      const snapshot = await db.getRef('feedbacks').orderByChild('userId').equalTo(parseInt(userId)).once('value')
      const feedbacks = snapshot.val()

      if (!feedbacks) {
        return []
      }

      return Object.keys(feedbacks).map(key => ({
        _id: key,
        ...feedbacks[key]
      }))
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.INTERNAL_SERVER_ERROR,
          `Tìm phản hồi theo user ID thất bại: ${error.message}`
        )
    }
  },

  /**
   * Lấy tất cả feedback với phân trang
   * @param {Object} query - Tham số truy vấn
   * @returns {Promise<Object>} - Danh sách feedback với phân trang
   * @throws {ApiError} - Nếu lấy danh sách thất bại
   */
  getAll: async (query = {}) => {
    try {
      const {
        page = 1,
        limit = 10,
        sortBy = 'createdAt',
        sortOrder = 'desc',
        status
      } = query

      const snapshot = await db.getRef('feedbacks').once('value')
      let feedbacks = snapshot.val()

      if (!feedbacks) {
        return {
          feedbacks: [],
          pagination: {
            page: parseInt(page),
            limit: parseInt(limit),
            total: 0,
            totalPages: 0
          }
        }
      }

      let feedbacksArray = Object.keys(feedbacks).map(key => ({
        _id: key,
        ...feedbacks[key]
      }))

      if (status) {
        feedbacksArray = feedbacksArray.filter(feedback => feedback.status === status)
      }

      feedbacksArray.sort((a, b) => {
        const aValue = a[sortBy] || 0
        const bValue = b[sortBy] || 0
        return sortOrder === 'asc' ? aValue - bValue : bValue - aValue
      })

      const total = feedbacksArray.length
      const totalPages = Math.ceil(total / limit)
      const startIndex = (page - 1) * limit
      const endIndex = startIndex + limit
      const paginatedFeedbacks = feedbacksArray.slice(startIndex, endIndex)

      return {
        feedbacks: paginatedFeedbacks,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total,
          totalPages
        }
      }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.INTERNAL_SERVER_ERROR,
          `Lấy danh sách phản hồi thất bại: ${error.message}`
        )
    }
  },

  /**
   * Cập nhật feedback
   * @param {number} feedbackId - ID feedback
   * @param {Object} updateData - Dữ liệu cập nhật
   * @returns {Promise<Object>} - Feedback đã cập nhật
   * @throws {ApiError} - Nếu cập nhật thất bại
   */
  update: async (feedbackId, updateData) => {
    try {
      if (!feedbackId) {
        throw new ApiError(
          httpStatus.BAD_REQUEST,
          'ID phản hồi là bắt buộc'
        )
      }

      const existingFeedback = await feedbackModel.findById(feedbackId)
      if (!existingFeedback) {
        throw new ApiError(
          httpStatus.NOT_FOUND,
          'Không tìm thấy phản hồi'
        )
      }

      const sanitizedData = {
        ...updateData,
        updatedAt: Date.now()
      }

      const feedbackRef = db.getRef(`feedbacks/${feedbackId}`)
      await feedbackRef.update(sanitizedData)

      return await feedbackModel.findById(feedbackId)
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.INTERNAL_SERVER_ERROR,
          `Cập nhật phản hồi thất bại: ${error.message}`
        )
    }
  },

  /**
   * Xóa feedback
   * @param {number} feedbackId - ID feedback
   * @returns {Promise<Object>} - Kết quả xóa
   * @throws {ApiError} - Nếu xóa thất bại
   */
  delete: async (feedbackId) => {
    try {
      if (!feedbackId) {
        throw new ApiError(
          httpStatus.BAD_REQUEST,
          'ID phản hồi là bắt buộc'
        )
      }

      const existingFeedback = await feedbackModel.findById(feedbackId)
      if (!existingFeedback) {
        throw new ApiError(
          httpStatus.NOT_FOUND,
          'Không tìm thấy phản hồi'
        )
      }

      const feedbackRef = db.getRef(`feedbacks/${feedbackId}`)
      await feedbackRef.remove()

      return {
        success: true,
        message: 'Xóa phản hồi thành công'
      }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.INTERNAL_SERVER_ERROR,
          `Xóa phản hồi thất bại: ${error.message}`
        )
    }
  },

  /**
   * Lấy thống kê feedback
   * @returns {Promise<Object>} - Thống kê feedback
   * @throws {ApiError} - Nếu lấy thống kê thất bại
   */
  getStats: async () => {
    try {
      const snapshot = await db.getRef('feedbacks').once('value')
      const feedbacks = snapshot.val()

      if (!feedbacks) {
        return {
          total: 0,
          pending: 0,
          resolved: 0,
          rejected: 0
        }
      }

      const feedbacksArray = Object.values(feedbacks)
      const stats = {
        total: feedbacksArray.length,
        pending: feedbacksArray.filter(f => f.status === 'pending').length,
        resolved: feedbacksArray.filter(f => f.status === 'resolved').length,
        rejected: feedbacksArray.filter(f => f.status === 'rejected').length
      }

      return stats
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.INTERNAL_SERVER_ERROR,
          `Lấy thống kê phản hồi thất bại: ${error.message}`
        )
    }
  }
}

module.exports = {
  create: feedbackModel.create,
  findById: feedbackModel.findById,
  findByUserId: feedbackModel.findByUserId,
  getAll: feedbackModel.getAll,
  update: feedbackModel.update,
  delete: feedbackModel.delete,
  getStats: feedbackModel.getStats
}
