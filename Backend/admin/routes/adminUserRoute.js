const express = require('express')
const auth = require('../../src/middlewares/authMiddleware')
const authorize = require('../../src/middlewares/authorize')
const validate = require('../../src/middlewares/validate')
const { adminUserValidation } = require('../validations/index')
const { adminUserController } = require('../controllers/index')

const router = express.Router()

router.use(auth, authorize('getUsers', 'deleteUser'))

// Tạo user mới
router.post(
  '/',
  validate(adminUserValidation.createUser),
  adminUserController.createUser
)

// Xóa mềm user
router.delete(
  '/:userId',
  validate(adminUserValidation.deleteUser),
  adminUserController.deleteUser
)

// Xóa vĩnh viễn user
router.delete(
  '/:userId/hard',
  validate(adminUserValidation.hardDeleteUser),
  adminUserController.hardDeleteUser
)

// Khôi phục user đã bị xóa mềm
router.post(
  '/:userId/restore',
  validate(adminUserValidation.restoreUser),
  adminUserController.restoreUser
)

// Lấy danh sách users đã bị xóa mềm
router.get(
  '/deleted',
  validate(adminUserValidation.getDeletedUsers),
  adminUserController.getDeletedUsers
)

// Lấy thống kê users
router.get(
  '/stats',
  validate(adminUserValidation.getUserStats),
  adminUserController.getUserStats
)

module.exports = router
