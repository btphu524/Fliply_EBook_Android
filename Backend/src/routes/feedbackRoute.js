const express = require('express')
const router = express.Router()
const feedbackController = require('../controllers/feedbackController')
const authMiddleware = require('../middlewares/authMiddleware')

router.post('/', authMiddleware, feedbackController.createFeedback)
router.get('/my-feedbacks', authMiddleware, feedbackController.getMyFeedbacks)
router.get('/:id', authMiddleware, feedbackController.getFeedbackById)
router.put('/:id', authMiddleware, feedbackController.updateFeedback)
router.delete('/:id', authMiddleware, feedbackController.deleteFeedback)

module.exports = router
