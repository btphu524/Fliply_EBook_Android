const express = require('express')
const auth = require('../middlewares/authMiddleware')
const validate = require('../middlewares/validate')
const bookValidation = require('../validations/bookValidation')
const bookController = require('../controllers/bookController')

const router = express.Router()

router.param('id', (req, res, next, id) => {
  if (!/^\d+$/.test(id)) return next('route')
  next()
})

router.get(
  '/',
  validate(bookValidation.getList),
  bookController.getList
)

router.get(
  '/latest',
  validate(bookValidation.getLatest),
  bookController.getLatest
)

router.get(
  '/search',
  validate(bookValidation.quickSearch),
  bookController.search
)

router.get(
  '/:id',
  validate(bookValidation.getById),
  bookController.getById
)

module.exports = router
