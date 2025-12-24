const { adminAuditMiddleware, adminRateLimitMiddleware, adminPermissionMiddleware } = require('./adminAuditMiddleware')

module.exports = {
  adminAuditMiddleware,
  adminRateLimitMiddleware,
  adminPermissionMiddleware
}
