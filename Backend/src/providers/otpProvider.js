const config = require('../config/config')
const logger = require('../config/logger')
const { db } = require('../config/db')
const { createEmailKey } = require('../utils/emailUtils')

const otpRef = db.ref('otps')

/**
 * Tạo OTP ngẫu nhiên
 * @param {number} length
 * @returns {string}
 */
function generate(length = config.otp.length) {
  const digits = '0123456789'
  let otp = ''
  for (let i = 0; i < length; i++) {
    otp += digits[Math.floor(Math.random() * 10)]
  }
  logger.info(`Generated OTP for length ${length}`)
  return otp
}

/**
 * Lưu trữ OTP trong Realtime Database
 * @param {string} email
 * @param {string} otp
 */
async function store(email, otp) {
  const key = createEmailKey(email)
  const data = {
    otp,
    attempts: 0,
    createdAt: Date.now(),
    expiresAt: Date.now() + config.otp.expiry * 1000
  }
  try {
    await otpRef.child(key).set(data)
    logger.info(`Stored OTP for ${email}`)
  } catch (error) {
    logger.error(`Failed to store OTP for ${email}: ${error.stack}`)
    throw error
  }
}

/**
 * Lấy OTP theo email
 * @param {string} email
 * @returns {Promise<object|null>}
 */
async function get(email) {
  const key = createEmailKey(email)
  try {
    const snapshot = await otpRef.child(key).once('value')
    const data = snapshot.val()
    if (!data) {
      logger.warn(`No OTP found for ${email}`)
    }
    return data
  } catch (error) {
    logger.error(`Failed to get OTP for ${email}: ${error.stack}`)
    throw error
  }
}

/**
 * Xác thực OTP
 * @param {string} email
 * @param {string} inputOTP
 * @returns {Promise<{success: boolean, message: string}>}
 */
async function verify(email, inputOTP) {
  const key = createEmailKey(email)
  const data = await get(email)

  // Kiểm tra OTP có tồn tại không
  if (!data) {
    logger.warn(
      `OTP verification failed for ${email}: OTP not found or expired`
    )
    return { success: false, message: 'OTP không tìm thấy hoặc đã hết hạn' }
  }

  // Kiểm tra OTP có hết hạn không
  if (Date.now() > data.expiresAt) {
    await remove(email)
    logger.warn(`OTP verification failed for ${email}: OTP expired`)
    return { success: false, message: 'OTP đã hết hạn' }
  }

  // Kiểm tra OTP có đúng không
  if (String(data.otp) !== String(inputOTP)) {
    try {
      await otpRef.child(key).update({ attempts: data.attempts + 1 })
      logger.warn(
        `OTP verification failed for ${email}: Incorrect OTP, attempt ${
          data.attempts + 1
        }`
      )
      return { success: false, message: 'Mã OTP không hợp lệ' }
    } catch (error) {
      logger.error(
        `Failed to update OTP attempts for ${email}: ${error.stack}`
      )
      throw error
    }
  }

  // Xác thực thành công, xóa OTP
  await remove(email)
  logger.info(`OTP verified successfully for ${email}`)
  return { success: true, message: 'Xác thực OTP thành công' }
}

/**
 * Xóa OTP
 * @param {string} email
 */
async function remove(email) {
  const key = createEmailKey(email)
  try {
    await otpRef.child(key).remove()
    logger.info(`Deleted OTP for ${email}`)
  } catch (error) {
    logger.error(`Failed to delete OTP for ${email}: ${error.stack}`)
    throw error
  }
}

module.exports = {
  generate,
  store,
  get,
  verify,
  delete: remove
}
