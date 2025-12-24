const { catchAsync } = require('../../src/utils/index')
const adminBookService = require('../services/adminBookService')

/**
 * Tạo sách mới (admin only)
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const createBook = catchAsync(async (req, res) => {
  const result = await adminBookService.createBook({ bookData: req.body })

  if (result.success) {
    res.status(201).json(result)
  } else {
    res.status(400).json(result)
  }
})

/**
 * Cập nhật sách (admin only)
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const updateBook = catchAsync(async (req, res) => {
  const { id } = req.params
  const result = await adminBookService.updateBookById({ id, updateData: req.body })

  if (result.success) {
    res.status(200).json(result)
  } else {
    // Kiểm tra nếu là lỗi không tìm thấy
    const isNotFound = result.message && result.message.includes('không tìm thấy')
    res.status(isNotFound ? 404 : 400).json(result)
  }
})

/**
 * Xóa mềm sách (admin only)
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const deleteBook = catchAsync(async (req, res) => {
  const { id } = req.params
  const result = await adminBookService.deleteBookById({ id })

  if (result.success) {
    res.status(200).json(result)
  } else {
    const isNotFound = result.message && result.message.includes('không tìm thấy')
    res.status(isNotFound ? 404 : 400).json(result)
  }
})

/**
 * Xóa vĩnh viễn sách (admin only)
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const hardDeleteBook = catchAsync(async (req, res) => {
  const { id } = req.params
  const result = await adminBookService.hardDeleteBookById({ id })

  if (result.success) {
    res.status(200).json(result)
  } else {
    const isNotFound = result.message && result.message.includes('không tìm thấy')
    res.status(isNotFound ? 404 : 400).json(result)
  }
})

/**
 * Khôi phục sách đã bị xóa mềm (admin only)
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const restoreBook = catchAsync(async (req, res) => {
  const { id } = req.params
  const result = await adminBookService.restoreBookById({ id })

  if (result.success) {
    res.status(200).json(result)
  } else {
    const isNotFound = result.message && result.message.includes('không tìm thấy')
    res.status(isNotFound ? 404 : 400).json(result)
  }
})

/**
 * Lấy danh sách sách đã bị xóa mềm (admin only)
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const getDeletedBooks = catchAsync(async (req, res) => {
  const result = await adminBookService.getDeletedBooks({ options: req.query })

  if (result.success) {
    res.status(200).json(result)
  } else {
    res.status(400).json(result)
  }
})

module.exports = {
  createBook,
  updateBook,
  deleteBook,
  hardDeleteBook,
  restoreBook,
  getDeletedBooks
}
