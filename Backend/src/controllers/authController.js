const httpStatus = require('http-status')
const { catchAsync } = require('../utils/index')
const { authService } = require('../services/index')

/**
 * Đăng ký người dùng mới
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const register = catchAsync(async (req, res) => {
  const result = await authService.SignUp(req.body)
  res.status(httpStatus.status.CREATED).json({
    success: true,
    data: { userId: result.userId },
    message: result.message
  })
})

/**
 * Xác thực OTP (tự động phát hiện loại dựa trên trạng thái user)
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const verifyOTP = catchAsync(async (req, res) => {
  const { email, otp } = req.body
  const result = await authService.verifyOTP(email, otp)
  res.json({
    success: true,
    message: result.message
  })
})

/**
 * Gửi lại OTP
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const resendOTP = catchAsync(async (req, res) => {
  const { email } = req.body
  const result = await authService.resendOTP(email)
  res.json({
    success: result.success,
    message: result.message
  })
})


/**
 * Đăng nhập người dùng
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const login = catchAsync(async (req, res) => {
  const { email, password } = req.body
  const result = await authService.login(email, password)
  res.json({
    success: true,
    data: {
      user: result.user,
      accessToken: result.accessToken,
      refreshToken: result.refreshToken
    },
    message: result.message
  })
})

/**
 * Quên mật khẩu - Bước 1: Gửi OTP đến email
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const forgotPassword = catchAsync(async (req, res) => {
  const { email } = req.body
  const result = await authService.forgotPassword(email)
  res.json({
    success: result.success,
    message: result.message
  })
})

/**
 * Đặt lại mật khẩu
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const resetPassword = catchAsync(async (req, res) => {
  const { email, newPassword, confirmPassword } = req.body
  const result = await authService.resetPassword(email, newPassword, confirmPassword)
  res.json({
    success: result.success,
    message: result.message
  })
})

/**
 * Đổi mật khẩu (yêu cầu đăng nhập)
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const changePassword = catchAsync(async (req, res) => {
  const { oldPassword, newPassword, confirmPassword } = req.body
  const userId = req.userId
  const result = await authService.changePassword(userId, oldPassword, newPassword, confirmPassword)
  res.json({
    success: result.success,
    message: result.message
  })
})

/**
 * Đăng xuất người dùng
 * @param {Object} req - HTTP request
 * @param {Object} res - HTTP response
 */
const logout = catchAsync(async (req, res) => {
  const { email } = req.body
  await authService.logout(email)
  res.json({ success: true, message: 'Đăng xuất thành công' })
})

module.exports = {
  register,
  verifyOTP,
  resendOTP,
  login,
  forgotPassword,
  resetPassword,
  changePassword,
  logout
}
