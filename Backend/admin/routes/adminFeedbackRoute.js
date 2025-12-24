const express = require('express')
const router = express.Router()
const adminFeedbackController = require('../controllers/adminFeedbackController')
const adminFeedbackValidation = require('../validations/adminFeedbackValidation')
const auth = require('../../src/middlewares/authMiddleware')
const validate = require('../../src/middlewares/validate')
const authorize = require('../../src/middlewares/authorize')

router.use(auth, authorize('getUsers'))

router.get('/',
  validate({ query: adminFeedbackValidation.getAllFeedbacksQuery }),
  adminFeedbackController.getAllFeedbacks
)

module.exports = router
