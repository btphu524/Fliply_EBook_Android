const httpStatus = require('http-status')
const logger = require('../config/logger')
const { ApiError, comparePassword } = require('../utils/index')
const { userModel } = require('../models/index')
const getOtpService = () => require('./otpService')
const getFirebaseService = () => require('./firebaseService')
const getTokenService = () => require('./tokenService')

/**
 * Đăng ký người dùng mới
 * @param {Object} userBody - Dữ liệu người dùng
 * @returns {Promise<Object>} - ID người dùng và thông báo
 * @throws {ApiError} - Nếu đăng ký thất bại
 */
const SignUp = async (userBody) => {
  try {
    const {
      email,
      password,
      confirmPassword,
      fullName,
      phoneNumber,
      role = 'user',
      _id,
      userId
    } = userBody

    const userData = {
      email,
      password,
      confirmPassword,
      fullName,
      phoneNumber,
      role,
      _id,
      userId
    }

    try {
      await userModel.findByEmailForActivation(userData.email)
      throw new ApiError(
        httpStatus.status.BAD_REQUEST,
        'Email đã được sử dụng'
      )
    } catch (error) {
      if (error instanceof ApiError && error.statusCode === httpStatus.status.NOT_FOUND) {
        // Email không tồn tại, tiếp tục
      } else {
        throw error
      }
    }

    await getFirebaseService().createAuthUser({ email: userData.email, password: userData.password })
    const { userId: createdUserId, message } = await userModel.create(userData)
    await getOtpService().sendOTP({ email: userData.email, type: 'register' })

    return { userId: createdUserId, message }
  } catch (error) {
    logger.error(`Error registering user: ${error.stack}`)
    throw error instanceof ApiError
      ? error
      : new ApiError(
        httpStatus.status.INTERNAL_SERVER_ERROR,
        `Đăng ký thất bại: ${error.message}`
      )
  }
}

/**
 * Xác thực OTP (tự động phát hiện loại dựa trên trạng thái user)
 * @param {string} email - Email người dùng
 * @param {string} otp - Mã OTP
 * @returns {Promise<Object>} - Kết quả xác thực
 * @throws {ApiError} - Nếu xác thực thất bại
 */
const verifyOTP = async (email, otp) => {
  try {
    const otpResult = await getOtpService().verifyOTP({ email, otp })
    if (!otpResult.success) {
      throw new ApiError(httpStatus.status.BAD_REQUEST, otpResult.message)
    }

    try {
      await userModel.findByEmail(email)
      return { success: true, message: 'Xác thực OTP thành công, bạn có thể đặt mật khẩu mới' }
    } catch (error) {
      if (error.statusCode === httpStatus.status.NOT_FOUND) {
        try {
          const inactiveUser = await userModel.findByEmailForActivation(email)
          const userId = inactiveUser._id
          await userModel.activateUser(userId)
          await getOtpService().clearOTP({ email })
          return { success: true, message: 'Tài khoản đã được kích hoạt thành công' }
        } catch (inactiveError) {
          throw new ApiError(
            httpStatus.status.NOT_FOUND,
            'Không tìm thấy tài khoản nào liên quan đến email này'
          )
        }
      }
      throw error
    }
  } catch (error) {
    logger.error(`Error verifying OTP for ${email}: ${error.stack}`)
    throw error instanceof ApiError
      ? error
      : new ApiError(
        httpStatus.status.INTERNAL_SERVER_ERROR,
        `Xác thực OTP thất bại: ${error.message}`
      )
  }
}

/**
 * Gửi lại OTP
 * @param {string} email - Email người dùng
 * @returns {Promise<Object>} - Kết quả gửi OTP
 * @throws {ApiError} - Nếu gửi thất bại
 */
const resendOTP = async (email) => {
  try {
    await getOtpService().sendOTP({ email, type: 'register' })
    return {
      success: true,
      message: 'Mã OTP đã được gửi lại đến email của bạn'
    }
  } catch (error) {
    logger.error(`Error resending OTP for ${email}: ${error.stack}`)
    throw error instanceof ApiError
      ? error
      : new ApiError(
        httpStatus.status.INTERNAL_SERVER_ERROR,
        `Gửi lại OTP thất bại: ${error.message}`
      )
  }
}


/**
 * Đăng nhập người dùng
 * @param {string} email - Email người dùng
 * @param {string} password - Mật khẩu người dùng
 * @returns {Promise<Object>} - Kết quả đăng nhập với dữ liệu người dùng và token
 * @throws {ApiError} - Nếu đăng nhập thất bại
 */
const login = async (email, password) => {
  try {
    logger.info(`Attempting login for email: ${email}`)
    let user
    try {
      user = await userModel.findByEmail(email)
      logger.info(`User found: ${user._id}, isActive: ${user.isActive}`)
    } catch (error) {
      logger.error(`Error finding user: ${error.message}`)
      if (error.statusCode === httpStatus.status.NOT_FOUND) {
        if (error.message === 'User not found or not activated') {
          throw new ApiError(
            httpStatus.status.UNAUTHORIZED,
            'Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email để kích hoạt tài khoản.'
          )
        } else {
          throw new ApiError(
            httpStatus.status.UNAUTHORIZED,
            'Email hoặc mật khẩu không đúng'
          )
        }
      }
      throw error
    }

    logger.info(`Comparing password for user: ${user._id}`)
    const isPasswordValid = await comparePassword(password, user.password)
    logger.info(`Password valid: ${isPasswordValid}`)
    if (!isPasswordValid) {
      logger.error(`Invalid password for user: ${user._id}`)
      throw new ApiError(
        httpStatus.status.UNAUTHORIZED,
        'Email hoặc mật khẩu không đúng'
      )
    }

    const accessToken = getTokenService().generateAccessToken({ userId: user._id, role: user.role })
    const refreshToken = getTokenService().generateRefreshToken({ userId: user._id })

    await userModel.update(user._id, {
      isOnline: true,
      lastLogin: Date.now()
    })

    const userWithoutPassword = { ...user }
    delete userWithoutPassword.password

    logger.info(`User ${user._id} logged in successfully`)

    return {
      user: userWithoutPassword,
      accessToken,
      refreshToken,
      message: 'Đăng nhập thành công'
    }
  } catch (error) {
    logger.error(`Error logging in user ${email}: ${error.stack}`)
    throw error instanceof ApiError
      ? error
      : new ApiError(
        httpStatus.status.INTERNAL_SERVER_ERROR,
        `Đăng nhập thất bại: ${error.message}`
      )
  }
}

/**
 * Quên mật khẩu
 * @param {string} email - Email người dùng
 * @returns {Promise<Object>} - Kết quả quên mật khẩu
 * @throws {ApiError} - Nếu quên mật khẩu thất bại
 */
const forgotPassword = async (email) => {
  try {
    try {
      await userModel.findByEmail(email)
    } catch (error) {
      if (error.statusCode === httpStatus.status.NOT_FOUND) {
        throw new ApiError(
          httpStatus.status.NOT_FOUND,
          'Email không tồn tại trong hệ thống'
        )
      }
      throw error
    }

    await getOtpService().sendOTP({ email, type: 'reset' })

    return {
      success: true,
      message: 'Mã OTP đã được gửi đến email của bạn'
    }
  } catch (error) {
    logger.error(`Error forgot password for ${email}: ${error.stack}`)
    throw error instanceof ApiError
      ? error
      : new ApiError(
        httpStatus.status.INTERNAL_SERVER_ERROR,
        `Quên mật khẩu thất bại: ${error.message}`
      )
  }
}


/**
 * Đặt lại mật khẩu
 * @param {string} email - Email người dùng
 * @param {string} newPassword - Mật khẩu mới
 * @param {string} confirmPassword - Xác nhận mật khẩu
 * @returns {Promise<Object>} - Kết quả đặt lại mật khẩu
 * @throws {ApiError} - Nếu đặt lại mật khẩu thất bại
 */
const resetPassword = async (email, newPassword, confirmPassword) => {
  try {
    if (newPassword !== confirmPassword) {
      throw new ApiError(
        httpStatus.status.BAD_REQUEST,
        'Mật khẩu mới và xác nhận mật khẩu không khớp'
      )
    }

    await userModel.findByEmail(email)

    await getFirebaseService().updateAuthUserPassword({ email, newPassword })
    await userModel.updatePassword(email, newPassword)
    await getOtpService().clearOTP({ email })

    return {
      success: true,
      message: 'Đặt lại mật khẩu thành công'
    }
  } catch (error) {
    logger.error(`Error resetting password for ${email}: ${error.stack}`)
    throw error instanceof ApiError
      ? error
      : new ApiError(
        httpStatus.status.INTERNAL_SERVER_ERROR,
        `Đặt lại mật khẩu thất bại: ${error.message}`
      )
  }
}

/**
 * Đổi mật khẩu (yêu cầu đăng nhập)
 * @param {string} userId - ID người dùng
 * @param {string} oldPassword - Mật khẩu cũ
 * @param {string} newPassword - Mật khẩu mới
 * @param {string} confirmPassword - Xác nhận mật khẩu mới
 * @returns {Promise<Object>} - Kết quả đổi mật khẩu
 * @throws {ApiError} - Nếu đổi mật khẩu thất bại
 */
const changePassword = async (userId, oldPassword, newPassword, confirmPassword) => {
  try {
    if (newPassword !== confirmPassword) {
      throw new ApiError(
        httpStatus.status.BAD_REQUEST,
        'Mật khẩu mới và xác nhận mật khẩu không khớp'
      )
    }

    const user = await userModel.findById(userId)
    if (!user) {
      throw new ApiError(
        httpStatus.status.NOT_FOUND,
        'Không tìm thấy người dùng'
      )
    }

    const isOldPasswordValid = await comparePassword(oldPassword, user.password)
    if (!isOldPasswordValid) {
      throw new ApiError(
        httpStatus.status.BAD_REQUEST,
        'Mật khẩu cũ không đúng'
      )
    }

    const isSamePassword = await comparePassword(newPassword, user.password)
    if (isSamePassword) {
      throw new ApiError(
        httpStatus.status.BAD_REQUEST,
        'Mật khẩu mới phải khác mật khẩu cũ'
      )
    }

    await getFirebaseService().updateAuthUserPassword({ email: user.email, newPassword })
    await userModel.updatePassword(user.email, newPassword)

    return {
      success: true,
      message: 'Đổi mật khẩu thành công'
    }
  } catch (error) {
    logger.error(`Error changing password for user ${userId}: ${error.stack}`)
    throw error instanceof ApiError
      ? error
      : new ApiError(
        httpStatus.status.INTERNAL_SERVER_ERROR,
        `Đổi mật khẩu thất bại: ${error.message}`
      )
  }
}

/**
 * Đăng xuất người dùng
 * @param {string} email - Email người dùng
 * @returns {Promise<void>} - Kết quả đăng xuất
 * @throws {ApiError} - Nếu đăng xuất thất bại
 */
const logout = async (email) => {
  try {
    const user = await userModel.findByEmail(email)
    if (!user) {
      throw new ApiError(
        httpStatus.status.NOT_FOUND,
        'Email không tồn tại trong hệ thống'
      )
    }

    await userModel.update(user._id, {
      isOnline: false,
      lastLogout: Date.now()
    })

    await getTokenService().revoke(user._id)
  } catch (error) {
    logger.error(`Error logging out user ${email}: ${error.stack}`)
    throw error
  }
}

module.exports = {
  SignUp,
  verifyOTP,
  resendOTP,
  login,
  forgotPassword,
  resetPassword,
  changePassword,
  logout
}
