const httpStatus = require('http-status')
const { ApiError } = require('../utils/index')
const { categoryModel } = require('../models/index')

/**
 * Lấy tất cả thể loại
 * @returns {Promise<Object>} - Danh sách thể loại và thông báo
 * @throws {ApiError} - Nếu lấy danh sách thất bại
 */
const getAllCategories = async () => {
  try {
    const categories = await categoryModel.findAll()
    return {
      success: true,
      data: { categories },
      message: 'Lấy danh sách thể loại thành công'
    }
  } catch (error) {
    if (error instanceof ApiError) throw error
    throw new ApiError(
      httpStatus.INTERNAL_SERVER_ERROR,
      `Lấy danh sách thể loại thất bại: ${error.message}`
    )
  }
}

/**
 * Lấy thể loại theo ID
 * @param {string} categoryId - ID thể loại
 * @returns {Promise<Object>} - Thông tin thể loại
 * @throws {ApiError} - Nếu lấy thể loại thất bại
 */
const getCategoryById = async (categoryId) => {
  try {
    const category = await categoryModel.findById(categoryId)
    return {
      success: true,
      data: { category },
      message: 'Lấy thể loại thành công'
    }
  } catch (error) {
    if (error instanceof ApiError) throw error
    throw new ApiError(
      httpStatus.INTERNAL_SERVER_ERROR,
      `Lấy thể loại thất bại: ${error.message}`
    )
  }
}

/**
 * Lấy ID hiện tại lớn nhất của category
 * @returns {Promise<Object>} - ID lớn nhất và thông báo
 * @throws {ApiError} - Nếu lấy ID thất bại
 */
const getCurrentMaxCategoryId = async () => {
  try {
    const result = await categoryModel.getCurrentMaxId()
    return {
      success: true,
      data: { maxId: result },
      message: 'Lấy ID lớn nhất thành công'
    }
  } catch (error) {
    if (error instanceof ApiError) throw error
    throw new ApiError(
      httpStatus.INTERNAL_SERVER_ERROR,
      `Lấy ID lớn nhất thất bại: ${error.message}`
    )
  }
}

module.exports = {
  getAllCategories,
  getCategoryById,
  getCurrentMaxCategoryId
}
