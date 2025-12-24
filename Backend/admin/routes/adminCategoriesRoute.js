const express = require('express')
const auth = require('../../src/middlewares/authMiddleware')
const authorize = require('../../src/middlewares/authorize')
const validate = require('../../src/middlewares/validate')
const { adminCategoriesValidation } = require('../validations/index')
const { adminCategoriesController } = require('../controllers/index')

const router = express.Router()

router.use(auth, authorize('createCategory', 'updateCategory', 'deleteCategory'))

// Tạo category mới
router.post(
  '/',
  validate(adminCategoriesValidation.createCategory),
  adminCategoriesController.createCategory
)

// Cập nhật category
router.put(
  '/:categoryId',
  validate(adminCategoriesValidation.updateCategory),
  adminCategoriesController.updateCategory
)

// Xóa mềm category
router.delete(
  '/:categoryId',
  validate(adminCategoriesValidation.deleteCategory),
  adminCategoriesController.deleteCategory
)

// Xóa vĩnh viễn category
router.delete(
  '/:categoryId/hard',
  validate(adminCategoriesValidation.hardDeleteCategory),
  adminCategoriesController.hardDeleteCategory
)

// Khôi phục category đã bị xóa mềm
router.post(
  '/:categoryId/restore',
  validate(adminCategoriesValidation.restoreCategory),
  adminCategoriesController.restoreCategory
)

// Lấy danh sách categories đã bị xóa mềm
router.get(
  '/deleted',
  validate(adminCategoriesValidation.getDeletedCategories),
  adminCategoriesController.getDeletedCategories
)

module.exports = router
