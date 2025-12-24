const { catchAsync } = require('../../src/utils/index')
const adminUserService = require('../services/adminUserService')

/**
 * Tạo user mới
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const createUser = catchAsync(async (req, res) => {
  const result = await adminUserService.createUser(req.body)

  if (result.success) {
    res.status(201).json(result)
  } else {
    res.status(400).json(result)
  }
})

/**
 * Xóa mềm user
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const deleteUser = catchAsync(async (req, res) => {
  const { userId } = req.params
  const result = await adminUserService.deleteUserById({ userId })

  if (result.success) {
    res.status(200).json(result)
  } else {
    const isNotFound = result.message && result.message.includes('không tìm thấy')
    res.status(isNotFound ? 404 : 400).json(result)
  }
})

/**
 * Xóa vĩnh viễn user
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const hardDeleteUser = catchAsync(async (req, res) => {
  const { userId } = req.params
  const result = await adminUserService.hardDeleteUserById({ userId })

  if (result.success) {
    res.status(200).json(result)
  } else {
    const isNotFound = result.message && result.message.includes('không tìm thấy')
    res.status(isNotFound ? 404 : 400).json(result)
  }
})

/**
 * Khôi phục user đã bị xóa mềm
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const restoreUser = catchAsync(async (req, res) => {
  const { userId } = req.params
  const result = await adminUserService.restoreUserById({ userId })

  if (result.success) {
    res.status(200).json(result)
  } else {
    const isNotFound = result.message && result.message.includes('không tìm thấy')
    res.status(isNotFound ? 404 : 400).json(result)
  }
})

/**
 * Lấy danh sách users đã bị xóa mềm
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const getDeletedUsers = catchAsync(async (req, res) => {
  const result = await adminUserService.getDeletedUsers({ options: req.query })

  if (result.success) {
    res.status(200).json(result)
  } else {
    res.status(400).json(result)
  }
})

/**
 * Lấy thống kê users
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const getUserStats = catchAsync(async (req, res) => {
  const result = await adminUserService.getUserStats()

  if (result.success) {
    res.status(200).json(result)
  } else {
    res.status(400).json(result)
  }
})

module.exports = {
  createUser,
  deleteUser,
  hardDeleteUser,
  restoreUser,
  getDeletedUsers,
  getUserStats
}
