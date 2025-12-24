const { feedbackModel } = require('../../src/models/index')
const { ApiError } = require('../../src/utils/index')

/**
 * Lấy tất cả đánh giá của người dùng cho admin
 * @param {Object} query - Tham số truy vấn
 * @returns {Promise<Object>} - Kết quả lấy danh sách feedback
 */
const getAllFeedbacks = async (query = {}) => {
  try {
    const result = await feedbackModel.getAll(query)

    return {
      success: true,
      message: 'Lấy danh sách đánh giá thành công',
      data: result.feedbacks,
      pagination: result.pagination
    }
  } catch (error) {
    return {
      success: false,
      message: error.message || 'Lấy danh sách đánh giá thất bại',
      error: error instanceof ApiError ? error.message : 'Lỗi hệ thống'
    }
  }
}

module.exports = {
  getAllFeedbacks
}
