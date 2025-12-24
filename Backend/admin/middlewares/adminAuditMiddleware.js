const httpStatus = require('http-status')
const { ApiError } = require('../../src/utils/index')
const logger = require('../../src/config/logger')

/**
 * Ghi log tất cả các thao tác admin để audit trail
 * @param {string} action - Tên action đang thực hiện
 * @returns {Function} Express middleware function
 *
 * @example
 * router.post('/books', adminAuditMiddleware('CREATE_BOOK'), controller.create)
 */
const adminAuditMiddleware = (action) => {
  return async (req, res, next) => {
    try {
      // Ghi log trước khi thực hiện action
      const auditData = {
        action,
        adminId: req.userId,
        adminRole: req.userRole,
        timestamp: new Date().toISOString(),
        method: req.method,
        url: req.originalUrl,
        ip: req.ip || req.connection.remoteAddress,
        userAgent: req.get('User-Agent'),
        body: req.method !== 'GET' ? req.body : undefined,
        params: req.params,
        query: req.query
      }

      logger.info(`Admin Action: ${action}`, auditData)

      req.auditData = auditData

      next()
    } catch (error) {
      logger.error(`Admin Audit Middleware Error: ${error.message}`)
      next(
        new ApiError(
          httpStatus.INTERNAL_SERVER_ERROR,
          'Audit logging failed'
        )
      )
    }
  }
}

/**
 * Giới hạn số lượng request admin để tránh abuse
 */
const adminRateLimitMiddleware = (windowMs = 15 * 60 * 1000, max = 100) => {
  const requests = new Map()

  return (req, res, next) => {
    const adminId = req.userId
    const now = Date.now()
    const windowStart = now - windowMs

    if (requests.has(adminId)) {
      const userRequests = requests.get(adminId).filter(time => time > windowStart)
      requests.set(adminId, userRequests)
    } else {
      requests.set(adminId, [])
    }

    const userRequests = requests.get(adminId)

    if (userRequests.length >= max) {
      return res.status(429).json({
        success: false,
        message: 'Quá nhiều request admin, vui lòng thử lại sau'
      })
    }

    userRequests.push(now)
    next()
  }
}

/**
 * Kiểm tra quyền cụ thể cho từng action
 */
const adminPermissionMiddleware = (requiredPermission) => {
  return async (req, res, next) => {
    try {
      if (requiredPermission === 'DELETE_USER' && req.params.userId === req.userId) {
        throw new ApiError(
          httpStatus.FORBIDDEN,
          'Không thể xóa chính mình'
        )
      }

      next()
    } catch (error) {
      next(
        error instanceof ApiError
          ? error
          : new ApiError(httpStatus.FORBIDDEN, 'Permission denied')
      )
    }
  }
}

module.exports = {
  adminAuditMiddleware,
  adminRateLimitMiddleware,
  adminPermissionMiddleware
}
