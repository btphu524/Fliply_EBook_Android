const { catchAsync } = require('../../src/utils/index')
const { adminFeedbackService } = require('../services/index')

/**
 * Lấy tất cả đánh giá của người dùng cho admin
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const getAllFeedbacks = catchAsync(async (req, res) => {
  const result = await adminFeedbackService.getAllFeedbacks(req.query)

  if (result.success) {
    res.status(200).json(result)
  } else {
    res.status(400).json(result)
  }
})

module.exports = {
  getAllFeedbacks
}
