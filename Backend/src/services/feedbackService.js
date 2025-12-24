const { feedbackModel, userModel } = require('../models/index')
const { ApiError } = require('../utils/index')
const httpStatus = require('http-status')

/**
 * Tạo feedback mới
 * @param {number} userId - ID người dùng
 * @param {Object} feedbackData - Dữ liệu feedback
 * @returns {Promise<Object>} - Kết quả tạo feedback
 * @throws {ApiError} - Nếu tạo feedback thất bại
 */
const createFeedback = async (userId, feedbackData) => {
  try {
    await userModel.findById(userId)

    const newFeedback = await feedbackModel.create({
      userId,
      fullName: feedbackData.fullName,
      phoneNumber: feedbackData.phoneNumber,
      email: feedbackData.email,
      comment: feedbackData.comment
    })

    return newFeedback
  } catch (error) {
    throw error instanceof ApiError
      ? error
      : new ApiError(httpStatus.INTERNAL_SERVER_ERROR, `Tạo feedback thất bại: ${error.message}`)
  }
}

/**
 * Lấy danh sách feedback với phân trang
 * @param {Object} query - Tham số truy vấn
 * @returns {Promise<Object>} - Danh sách feedback với phân trang
 * @throws {ApiError} - Nếu lấy danh sách thất bại
 */
const getFeedbacks = async (query) => {
  try {
    return await feedbackModel.getAll(query)
  } catch (error) {
    throw error instanceof ApiError
      ? error
      : new ApiError(httpStatus.INTERNAL_SERVER_ERROR, `Lấy danh sách feedback thất bại: ${error.message}`)
  }
}

/**
 * Lấy feedback theo ID
 * @param {number} feedbackId - ID feedback
 * @returns {Promise<Object>} - Thông tin feedback
 * @throws {ApiError} - Nếu không tìm thấy feedback
 */
const getFeedbackById = async (feedbackId) => {
  try {
    return await feedbackModel.findById(feedbackId)
  } catch (error) {
    throw error instanceof ApiError
      ? error
      : new ApiError(httpStatus.INTERNAL_SERVER_ERROR, `Lấy feedback thất bại: ${error.message}`)
  }
}

/**
 * Cập nhật feedback
 * @param {number} feedbackId - ID feedback
 * @param {Object} updateData - Dữ liệu cập nhật
 * @returns {Promise<Object>} - Feedback đã cập nhật
 * @throws {ApiError} - Nếu cập nhật thất bại
 */
const updateFeedback = async (feedbackId, updateData) => {
  try {
    return await feedbackModel.update(feedbackId, updateData)
  } catch (error) {
    throw error instanceof ApiError
      ? error
      : new ApiError(httpStatus.INTERNAL_SERVER_ERROR, `Cập nhật feedback thất bại: ${error.message}`)
  }
}

/**
 * Xóa feedback
 * @param {number} feedbackId - ID feedback
 * @returns {Promise<Object>} - Kết quả xóa
 * @throws {ApiError} - Nếu xóa thất bại
 */
const deleteFeedback = async (feedbackId) => {
  try {
    return await feedbackModel.delete(feedbackId)
  } catch (error) {
    throw error instanceof ApiError
      ? error
      : new ApiError(httpStatus.INTERNAL_SERVER_ERROR, `Xóa feedback thất bại: ${error.message}`)
  }
}

/**
 * Lấy feedback của user với phân trang
 * @param {number} userId - ID người dùng
 * @param {Object} query - Tham số truy vấn
 * @returns {Promise<Object>} - Feedback của user với phân trang
 * @throws {ApiError} - Nếu lấy feedback thất bại
 */
const getUserFeedbacks = async (userId, query) => {
  try {
    const userFeedbacks = await feedbackModel.findByUserId(userId)

    const page = parseInt(query.page) || 1
    const limit = parseInt(query.limit) || 10
    const startIndex = (page - 1) * limit
    const endIndex = startIndex + limit

    const paginatedFeedbacks = userFeedbacks.slice(startIndex, endIndex)
    const total = userFeedbacks.length
    const totalPages = Math.ceil(total / limit)

    return {
      feedbacks: paginatedFeedbacks,
      pagination: {
        page,
        limit,
        total,
        totalPages
      }
    }
  } catch (error) {
    throw error instanceof ApiError
      ? error
      : new ApiError(httpStatus.INTERNAL_SERVER_ERROR, `Lấy feedback của user thất bại: ${error.message}`)
  }
}

/**
 * Lấy thống kê feedback
 * @returns {Promise<Object>} - Thống kê feedback
 * @throws {ApiError} - Nếu lấy thống kê thất bại
 */
const getFeedbackStats = async () => {
  try {
    return await feedbackModel.getStats()
  } catch (error) {
    throw error instanceof ApiError
      ? error
      : new ApiError(httpStatus.INTERNAL_SERVER_ERROR, `Lấy thống kê feedback thất bại: ${error.message}`)
  }
}

module.exports = {
  createFeedback,
  getFeedbacks,
  getFeedbackById,
  updateFeedback,
  deleteFeedback,
  getUserFeedbacks,
  getFeedbackStats
}
