const express = require('express')
const auth = require('../../src/middlewares/authMiddleware')
const authorize = require('../../src/middlewares/authorize')
const validate = require('../../src/middlewares/validate')
const { adminBookValidation } = require('../validations/index')
const { adminBookController } = require('../controllers/index')

const router = express.Router()

router.use(auth, authorize('createBook', 'updateBook', 'deleteBook'))

router.param('id', (req, res, next, id) => {
  if (!/^\d+$/.test(id)) return next('route')
  next()
})

// Tạo sách mới
router.post(
  '/',
  validate(adminBookValidation.createBook),
  adminBookController.createBook
)

// Cập nhật sách
router.put(
  '/:id',
  validate(adminBookValidation.updateBook),
  adminBookController.updateBook
)

// Xóa mềm sách
router.delete(
  '/:id',
  validate(adminBookValidation.deleteBook),
  adminBookController.deleteBook
)

// Xóa vĩnh viễn sách
router.delete(
  '/:id/hard',
  validate(adminBookValidation.hardDeleteBook),
  adminBookController.hardDeleteBook
)

// Khôi phục sách đã bị xóa mềm
router.post(
  '/:id/restore',
  validate(adminBookValidation.restoreBook),
  adminBookController.restoreBook
)

// Lấy danh sách sách đã bị xóa mềm
router.get(
  '/deleted',
  validate(adminBookValidation.getDeletedBooks),
  adminBookController.getDeletedBooks
)

module.exports = router
