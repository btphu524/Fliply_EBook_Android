const { otpProvider } = require('../providers/index')
const emailService = require('./emailService')
const logger = require('../config/logger')

/**
 * Gửi OTP đến email theo loại yêu cầu
 * @param {Object} data - Dữ liệu gửi OTP
 * @param {string} data.email - Email người nhận
 * @param {'register'|'reset'|'update'} data.type - Loại OTP
 * @returns {Promise<Object>} - Kết quả gửi OTP
 * @throws {Error} - Nếu gửi OTP thất bại
 */
const sendOTP = async (data) => {
  const { email, type } = data
  if (!['register', 'reset', 'update'].includes(type)) {
    logger.error(`Invalid OTP type: ${type}`)
    throw new Error('Loại OTP không hợp lệ')
  }

  try {
    const otp = otpProvider.generate()
    await otpProvider.store(email, otp)

    const result = await emailService.sendOTP({ email, otp, type })
    logger.info(`OTP sent to ${email} for ${type}`)

    return { success: true, message: 'OTP đã được gửi thành công', data: result }
  } catch (error) {
    logger.error(`Failed to send OTP to ${email} for ${type}: ${error.stack}`)
    throw error
  }
}

/**
 * Xác thực OTP
 * @param {Object} data - Dữ liệu xác thực
 * @param {string} data.email - Email người dùng
 * @param {string} data.otp - Mã OTP
 * @returns {Promise<Object>} - Kết quả xác thực
 * @throws {Error} - Nếu xác thực thất bại
 */
const verifyOTP = async (data) => {
  const { email, otp } = data
  try {
    const result = await otpProvider.verify(email, otp)

    if (result.success) {
      logger.info(`OTP verified for ${email}`)
    } else {
      logger.warn(`OTP verification failed for ${email}: ${result.message}`)
    }

    return result
  } catch (error) {
    logger.error(`Error verifying OTP for ${email}: ${error.stack}`)
    return { success: false, message: 'Lỗi xác thực OTP' }
  }
}

/**
 * Xóa OTP đã lưu trữ
 * @param {Object} data - Dữ liệu xóa OTP
 * @param {string} data.email - Email người dùng
 * @returns {Promise<void>}
 * @throws {Error} - Nếu xóa OTP thất bại
 */
const clearOTP = async (data) => {
  const { email } = data
  try {
    await otpProvider.delete(email)
    logger.info(`Cleared OTP for ${email}`)
  } catch (error) {
    logger.error(`Failed to clear OTP for ${email}: ${error.stack}`)
    throw error
  }
}

module.exports = {
  sendOTP,
  verifyOTP,
  clearOTP
}
