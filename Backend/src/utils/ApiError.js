/**
 * Lớp lỗi tùy chỉnh cho API
 * @class ApiError
 * @extends Error
 */
class ApiError extends Error {
  /**
   * Tạo một lỗi API mới
   * @param {number} statusCode - Mã trạng thái HTTP
   * @param {string} message - Thông báo lỗi
   * @param {boolean} [isOperational=true] - Có phải lỗi hoạt động không
   * @param {string} [stack=''] - Stack trace
   */
  constructor(statusCode, message, isOperational = true, stack = '') {
    super(message)
    this.statusCode = statusCode
    this.isOperational = isOperational
    if (stack) {
      this.stack = stack
    } else {
      Error.captureStackTrace(this, this.constructor)
    }
  }
}

module.exports = ApiError
