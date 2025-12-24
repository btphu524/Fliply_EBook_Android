const express = require('express')
const historyController = require('../controllers/historyController')
const historyValidation = require('../validations/historyValidation')
const validate = require('../middlewares/validate')
const authenticate = require('../middlewares/authMiddleware')

const router = express.Router()

router.post(
  '/bookmark',
  authenticate,
  validate(historyValidation.saveBookmark),
  historyController.saveBookmark
)

router.get(
  '/:userId',
  authenticate,
  validate(historyValidation.getReadingHistory),
  historyController.getReadingHistory
)

router.get(
  '/:userId/bookmark/:bookId',
  authenticate,
  validate(historyValidation.getBookmark),
  historyController.getBookmark
)

router.delete(
  '/:userId/bookmark/:bookId',
  authenticate,
  validate(historyValidation.deleteBookmark),
  historyController.deleteBookmark
)


router.get(
  '/user/:userId',
  authenticate,
  validate(historyValidation.getHistoryByUser),
  historyController.getHistoryByUser
)

router.get(
  '/book/:bookId',
  validate(historyValidation.getHistoryByBook),
  historyController.getHistoryByBook
)


module.exports = router
