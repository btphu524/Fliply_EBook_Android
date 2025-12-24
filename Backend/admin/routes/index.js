const express = require('express')
const adminCategoriesRoute = require('./adminCategoriesRoute')
const adminBookRoute = require('./adminBookRoute')
const adminUserRoute = require('./adminUserRoute')
const adminFeedbackRoute = require('./adminFeedbackRoute')

const router = express.Router()

router.use('/categories', adminCategoriesRoute)
router.use('/books', adminBookRoute)
router.use('/users', adminUserRoute)
router.use('/feedbacks', adminFeedbackRoute)

module.exports = router
