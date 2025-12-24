const httpStatus = require('http-status')
const { ApiError } = require('../utils/index')
const { roleRights } = require('../config/role')
const { userService } = require('../services/index')

const authorize = (...requiredRights) => {
  return async (req, res, next) => {
    try {
      const user = await userService.getUserById({ id: req.userId })

      if (!user) {
        throw new ApiError(httpStatus.UNAUTHORIZED, 'User not found')
      }

      const userRole = user.role || 'user'
      const userRights = roleRights.get(userRole) || []

      const hasRequiredRights = requiredRights.every(right =>
        userRights.includes(right)
      )

      if (!hasRequiredRights) {
        throw new ApiError(
          httpStatus.FORBIDDEN,
          'You do not have permission to perform this action'
        )
      }

      req.userRole = userRole
      next()
    } catch (error) {
      next(
        error instanceof ApiError
          ? error
          : new ApiError(httpStatus.FORBIDDEN, 'Access denied')
      )
    }
  }
}

module.exports = authorize
