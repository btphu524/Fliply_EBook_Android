const feedbackService = require('../services/feedbackService')
const { createFeedback, updateFeedback, getFeedbacks, feedbackId } = require('../validations/feedbackValidation')
const catchAsync = require('../utils/catchAsync')

/**
 * Tạo feedback mới
 * @param {Object} req - Request object
 * @param {Object} res - Response object
 * @returns {Promise<void>}
 */
const createFeedbackController = catchAsync(async (req, res) => {
  const userId = req.userId
  const feedbackData = req.body

  const { error, value } = createFeedback.validate(feedbackData)
  if (error) {
    return res.status(400).json({
      success: false,
      message: 'Dữ liệu không hợp lệ',
      errors: error.details.map(detail => detail.message)
    })
  }

  const feedback = await feedbackService.createFeedback(userId, value)

  res.status(201).json({
    success: true,
    message: 'Gửi phản hồi thành công',
    data: feedback
  })
})

/**
 * Lấy danh sách feedback của user
 * @param {Object} req - Request object
 * @param {Object} res - Response object
 * @returns {Promise<void>}
 */
const getMyFeedbacks = catchAsync(async (req, res) => {
  const userId = req.userId
  const query = req.query

  const { error, value } = getFeedbacks.validate(query)
  if (error) {
    return res.status(400).json({
      success: false,
      message: 'Tham số truy vấn không hợp lệ',
      errors: error.details.map(detail => detail.message)
    })
  }

  const result = await feedbackService.getUserFeedbacks(userId, value)

  res.status(200).json({
    success: true,
    message: 'Lấy phản hồi của bạn thành công',
    data: result.feedbacks,
    pagination: result.pagination
  })
})

/**
 * Lấy feedback theo ID
 * @param {Object} req - Request object
 * @param {Object} res - Response object
 * @returns {Promise<void>}
 */
const getFeedbackById = catchAsync(async (req, res) => {
  const { id } = req.params
  const userId = req.userId

  const { error, value } = feedbackId.validate({ id: parseInt(id) })
  if (error) {
    return res.status(400).json({
      success: false,
      message: 'ID không hợp lệ',
      errors: error.details.map(detail => detail.message)
    })
  }

  const feedback = await feedbackService.getFeedbackById(value.id)

  if (feedback.userId !== parseInt(userId)) {
    return res.status(403).json({
      success: false,
      message: 'Bạn không có quyền xem phản hồi này'
    })
  }

  res.status(200).json({
    success: true,
    message: 'Lấy phản hồi thành công',
    data: feedback
  })
})

/**
 * Cập nhật feedback
 * @param {Object} req - Request object
 * @param {Object} res - Response object
 * @returns {Promise<void>}
 */
const updateFeedbackController = catchAsync(async (req, res) => {
  const { id } = req.params
  const userId = req.userId
  const updateData = req.body

  const { error: idError, value: idValue } = feedbackId.validate({ id: parseInt(id) })
  if (idError) {
    return res.status(400).json({
      success: false,
      message: 'ID không hợp lệ',
      errors: idError.details.map(detail => detail.message)
    })
  }

  const { error: dataError, value: dataValue } = updateFeedback.validate(updateData)
  if (dataError) {
    return res.status(400).json({
      success: false,
      message: 'Dữ liệu cập nhật không hợp lệ',
      errors: dataError.details.map(detail => detail.message)
    })
  }

  const existingFeedback = await feedbackService.getFeedbackById(idValue.id)
  if (existingFeedback.userId !== parseInt(userId)) {
    return res.status(403).json({
      success: false,
      message: 'Bạn không có quyền cập nhật phản hồi này'
    })
  }

  const feedback = await feedbackService.updateFeedback(idValue.id, dataValue)

  res.status(200).json({
    success: true,
    message: 'Cập nhật phản hồi thành công',
    data: feedback
  })
})

/**
 * Xóa feedback
 * @param {Object} req - Request object
 * @param {Object} res - Response object
 * @returns {Promise<void>}
 */
const deleteFeedback = catchAsync(async (req, res) => {
  const { id } = req.params
  const userId = req.userId

  const { error, value } = feedbackId.validate({ id: parseInt(id) })
  if (error) {
    return res.status(400).json({
      success: false,
      message: 'ID không hợp lệ',
      errors: error.details.map(detail => detail.message)
    })
  }

  const existingFeedback = await feedbackService.getFeedbackById(value.id)
  if (existingFeedback.userId !== parseInt(userId)) {
    return res.status(403).json({
      success: false,
      message: 'Bạn không có quyền xóa phản hồi này'
    })
  }

  const result = await feedbackService.deleteFeedback(value.id)

  res.status(200).json({
    success: true,
    message: result.message
  })
})

module.exports = {
  createFeedback: createFeedbackController,
  getMyFeedbacks,
  getFeedbackById,
  updateFeedback: updateFeedbackController,
  deleteFeedback
}
