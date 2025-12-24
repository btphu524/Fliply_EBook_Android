const historyService = require('../services/historyService')
const { ApiError, catchAsync } = require('../utils/index')
const httpStatus = require('http-status')

const historyController = {
  /**
   * Lưu bookmark cho người dùng
   * @param {Object} req - HTTP request
   * @param {Object} res - HTTP response
   * @returns {void}
   */
  saveBookmark: catchAsync(async (req, res) => {
    const { userId, bookId, chapterId } = req.body

    if (!userId || !bookId || chapterId === 'null') {
      throw new ApiError(
        httpStatus.status.BAD_REQUEST,
        'ID người dùng, ID sách và chapterId không được để trống'
      )
    }

    const result = await historyService.saveBookmark({
      userId,
      bookId,
      chapterId
    })

    res.status(httpStatus.status.OK).json(result)
  }),

  /**
   * Lấy lịch sử đọc của người dùng
   * @param {Object} req - HTTP request
   * @param {Object} res - HTTP response
   * @returns {void}
   */
  getReadingHistory: catchAsync(async (req, res) => {
    const { userId } = req.params
    const tokenUserId = req.userId
    const { page, limit, sortBy, sortOrder } = req.query

    // Kiểm tra user chỉ có thể xem lịch sử của chính mình
    if (parseInt(userId) !== parseInt(tokenUserId)) {
      throw new ApiError(
        httpStatus.status.FORBIDDEN,
        'Bạn chỉ có thể xem lịch sử đọc của chính mình'
      )
    }

    const options = {
      page: parseInt(page) || 1,
      limit: parseInt(limit) || 10,
      sortBy: sortBy || 'lastReadAt',
      sortOrder: sortOrder || 'desc'
    }

    const result = await historyService.getReadingHistory(userId, options)

    res.status(httpStatus.status.OK).json(result)
  }),

  /**
   * Lấy bookmark của người dùng cho một cuốn sách
   * @param {Object} req - HTTP request
   * @param {Object} res - HTTP response
   * @returns {void}
   */
  getBookmark: catchAsync(async (req, res) => {
    const { userId, bookId } = req.params
    const tokenUserId = req.userId

    // Kiểm tra user chỉ có thể xem bookmark của chính mình
    if (parseInt(userId) !== parseInt(tokenUserId)) {
      throw new ApiError(
        httpStatus.status.FORBIDDEN,
        'Bạn chỉ có thể xem bookmark của chính mình'
      )
    }

    const result = await historyService.getBookmark(userId, bookId)

    res.status(httpStatus.status.OK).json(result)
  }),

  /**
   * Xóa bookmark của người dùng
   * @param {Object} req - HTTP request
   * @param {Object} res - HTTP response
   * @returns {void}
   */
  deleteBookmark: catchAsync(async (req, res) => {
    const { userId, bookId } = req.params
    const tokenUserId = req.userId

    // Kiểm tra user chỉ có thể xóa bookmark của chính mình
    if (parseInt(userId) !== parseInt(tokenUserId)) {
      throw new ApiError(
        httpStatus.status.FORBIDDEN,
        'Bạn chỉ có thể xóa bookmark của chính mình'
      )
    }

    const result = await historyService.deleteBookmark(userId, bookId)

    res.status(httpStatus.status.OK).json(result)
  }),


  /**
   * Lấy lịch sử đọc theo người dùng
   * @param {Object} req - HTTP request
   * @param {Object} res - HTTP response
   * @returns {void}
   */
  getHistoryByUser: catchAsync(async (req, res) => {
    const { userId } = req.params
    const tokenUserId = req.userId

    // Kiểm tra user chỉ có thể xem lịch sử của chính mình
    if (parseInt(userId) !== parseInt(tokenUserId)) {
      throw new ApiError(
        httpStatus.status.FORBIDDEN,
        'Bạn chỉ có thể xem lịch sử đọc của chính mình'
      )
    }

    const result = await historyService.getHistoryByUser(userId)

    res.status(httpStatus.status.OK).json(result)
  }),

  /**
   * Lấy lịch sử đọc theo sách
   * @param {Object} req - HTTP request
   * @param {Object} res - HTTP response
   * @returns {void}
   */
  getHistoryByBook: catchAsync(async (req, res) => {
    const { bookId } = req.params

    const result = await historyService.getHistoryByBook(bookId)

    res.status(httpStatus.status.OK).json(result)
  })
}

module.exports = historyController
