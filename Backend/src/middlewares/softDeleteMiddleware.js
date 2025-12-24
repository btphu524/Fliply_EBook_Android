const httpStatus = require('http-status')
const { ApiError } = require('../utils/index')

/**
 * Middleware xử lý soft delete cho các entity
 * Thay vì xóa hẳn khỏi database, đánh dấu isActive = false
 *
 * @param {string} entityName - Tên entity (user, book, category, etc.)
 * @returns {Function} Express middleware function
 *
 * @example
 * router.delete('/:id', softDeleteMiddleware('book'), controller.delete)
 */
const softDeleteMiddleware = (entityName) => {
  return async (req, res, next) => {
    try {
      // Chỉ áp dụng cho DELETE requests
      if (req.method !== 'DELETE') {
        return next()
      }

      // Lấy ID từ params với nhiều pattern khác nhau
      const id = req.params[`${entityName}Id`] || req.params.id || req.params.userId

      if (!id) {
        throw new ApiError(
          httpStatus.BAD_REQUEST,
          `${entityName} ID is required`
        )
      }

      // Thêm thông tin soft delete vào request để controller sử dụng
      req.softDelete = {
        id,
        entityName,
        deletedAt: new Date().toISOString(),
        isActive: false
      }

      next()
    } catch (error) {
      next(
        error instanceof ApiError
          ? error
          : new ApiError(httpStatus.INTERNAL_SERVER_ERROR, 'Soft delete middleware error')
      )
    }
  }
}

module.exports = softDeleteMiddleware
