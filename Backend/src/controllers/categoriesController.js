const { catchAsync } = require('../utils/index')
const { categoriesService } = require('../services/index')

/**
 * Lấy tất cả thể loại
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const getAll = catchAsync(async (req, res) => {
  const result = await categoriesService.getAllCategories()
  res.status(200).json(result)
})

/**
 * Lấy thể loại theo ID
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const getById = catchAsync(async (req, res) => {
  const { categoryId } = req.params
  const result = await categoriesService.getCategoryById(categoryId)
  res.status(200).json(result)
})

module.exports = {
  getAll,
  getById
}
