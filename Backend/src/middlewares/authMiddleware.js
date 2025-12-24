const httpStatus = require('http-status')
const { ApiError } = require('../utils/index')
const { tokenService } = require('../services/index')
const logger = require('../config/logger')

const authMiddleware = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization
    if (!authHeader) {
      throw new ApiError(
        httpStatus.UNAUTHORIZED,
        'Authorization header is required'
      )
    }
    if (!authHeader.startsWith('Bearer ')) {
      throw new ApiError(
        httpStatus.UNAUTHORIZED,
        'Authorization header must start with Bearer '
      )
    }
    const token = authHeader.split(' ')[1]
    if (!token) {
      throw new ApiError(
        httpStatus.UNAUTHORIZED,
        'Token is required'
      )
    }
    const payload = tokenService.verifyToken({ token })
    req.userId = payload.sub
    next()
  } catch (error) {
    logger.error(`Token authentication error: ${error.stack}`)
    next(
      error instanceof ApiError
        ? error
        : new ApiError(httpStatus.UNAUTHORIZED, 'Invalid token')
    )
  }
}

module.exports = authMiddleware
