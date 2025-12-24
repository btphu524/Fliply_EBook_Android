const jwt = require('jsonwebtoken')
const NodeCache = require('node-cache')
const config = require('../config/config')
const logger = require('../config/logger')
const { auth } = require('../config/db')

// Initialize cache with safe default
let refreshTokenCache
try {
  refreshTokenCache = new NodeCache({
    stdTTL: (config && config.cache && config.cache.ttl) || 300
  })
} catch (error) {
  // Fallback if config is not available
  refreshTokenCache = new NodeCache({ stdTTL: 300 })
}

/**
 * Tạo access token
 * @param {Object} data - Dữ liệu tạo token
 * @param {string} data.userId - ID người dùng
 * @param {string} data.role - Vai trò người dùng
 * @returns {string} - Access token
 * @throws {Error} - Nếu tạo token thất bại
 */
const generateAccessToken = (data) => {
  const { userId, role } = data
  try {
    const token = jwt.sign({ sub: userId, role }, config.jwt.secret, {
      expiresIn: config.jwt.expiry
    })
    logger.info(`Generated access token for user ${userId}`)
    return token
  } catch (error) {
    logger.error(
      `Failed to generate access token for user ${userId}: ${error.stack}`
    )
    throw error
  }
}

/**
 * Tạo refresh token
 * @param {Object} data - Dữ liệu tạo token
 * @param {string} data.userId - ID người dùng
 * @returns {string} - Refresh token
 * @throws {Error} - Nếu tạo token thất bại
 */
const generateRefreshToken = (data) => {
  const { userId } = data
  try {
    const refreshToken = jwt.sign({ sub: userId }, config.jwt.secret, {
      expiresIn: '30d'
    })
    refreshTokenCache.set(`refresh:${userId}`, refreshToken)
    logger.info(`Generated refresh token for user ${userId}`)
    return refreshToken
  } catch (error) {
    logger.error(
      `Failed to generate refresh token for user ${userId}: ${error.stack}`
    )
    throw error
  }
}

/**
 * Tạo Firebase custom token
 * @param {Object} data - Dữ liệu tạo token
 * @param {string} data.userId - ID người dùng
 * @param {Object} [data.additionalClaims={}] - Thông tin bổ sung
 * @returns {Promise<string>} - Firebase custom token
 * @throws {Error} - Nếu tạo token thất bại
 */
const generateFirebaseCustomToken = async (data) => {
  const { userId, additionalClaims = {} } = data
  try {
    const token = await auth
      .createCustomToken(userId, additionalClaims)
    logger.info(`Generated Firebase custom token for user ${userId}`)
    return token
  } catch (error) {
    logger.error(
      `Failed to generate Firebase custom token for user ${userId}: ${error.stack}`
    )
    throw error
  }
}

/**
 * Xác thực JWT token
 * @param {Object} data - Dữ liệu xác thực
 * @param {string} data.token - JWT token
 * @returns {Object|null} - Thông tin token đã giải mã
 * @throws {Error} - Nếu token không hợp lệ
 */
const verifyToken = (data) => {
  const { token } = data
  try {
    const decoded = jwt.verify(token, config.jwt.secret)
    logger.info(`Verified token for user ${decoded.sub}`)
    return decoded
  } catch (error) {
    logger.error(`Token verification failed: ${error.stack}`)
    throw new Error('Invalid token')
  }
}

/**
 * Làm mới access token bằng refresh token
 * @param {Object} data - Dữ liệu làm mới token
 * @param {string} data.refreshToken - Refresh token
 * @returns {Promise<Object>} - Access token và refresh token mới
 * @throws {Error} - Nếu làm mới token thất bại
 */
const refresh = async (data) => {
  const { refreshToken } = data
  try {
    const decoded = verifyToken({ token: refreshToken })
    if (!decoded) {
      logger.warn('Invalid refresh token')
      throw new Error('Invalid refresh token')
    }
    const cached = refreshTokenCache.get(`refresh:${decoded.sub}`)
    if (cached !== refreshToken) {
      logger.warn(`Refresh token mismatch for user ${decoded.sub}`)
      throw new Error('Invalid refresh token')
    }
    const newAccessToken = generateAccessToken({ userId: decoded.sub, role: 'user' })
    logger.info(`Refreshed tokens for user ${decoded.sub}`)
    return { accessToken: newAccessToken, refreshToken }
  } catch (error) {
    logger.error(`Failed to refresh token: ${error.stack}`)
    throw error
  }
}

/**
 * Thu hồi refresh token
 * @param {Object} data - Dữ liệu thu hồi token
 * @param {string} data.userId - ID người dùng
 * @returns {void}
 * @throws {Error} - Nếu thu hồi token thất bại
 */
const revoke = (data) => {
  const { userId } = data
  try {
    refreshTokenCache.del(`refresh:${userId}`)
    logger.info(`Revoked refresh token for user ${userId}`)
  } catch (error) {
    logger.error(
      `Failed to revoke refresh token for user ${userId}: ${error.stack}`
    )
    throw error
  }
}

module.exports = {
  generateAccessToken,
  generateRefreshToken,
  generateFirebaseCustomToken,
  verifyToken,
  refresh,
  revoke
}
