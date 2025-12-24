const express = require('express')
const auth = require('../middlewares/authMiddleware')
const validate = require('../middlewares/validate')
const authValidation = require('../validations/authValidation')
const authController = require('../controllers/authController')

const router = express.Router()

router.post(
  '/register',
  validate(authValidation.register),
  authController.register
)

router.post(
  '/verify-otp',
  validate(authValidation.verifyOTP),
  authController.verifyOTP
)

router.post(
  '/resend-otp',
  validate(authValidation.resendOTP),
  authController.resendOTP
)


router.post(
  '/login',
  validate(authValidation.login),
  authController.login
)

router.post(
  '/forgot-password',
  validate(authValidation.forgotPassword),
  authController.forgotPassword
)


router.post(
  '/reset-password',
  validate(authValidation.resetPassword),
  authController.resetPassword
)

router.post(
  '/change-password',
  auth,
  validate(authValidation.changePassword),
  authController.changePassword
)

router.post(
  '/logout',
  validate(authValidation.logout),
  authController.logout
)


module.exports = router
