const express = require('express')
const auth = require('../middlewares/authMiddleware')
const validate = require('../middlewares/validate')
const userValidation = require('../validations/userValidation')
const userController = require('../controllers/userController')

const router = express.Router()

router
  .route('/')
  .get(auth, validate(userValidation.getUser), userController.getUserByEmail)

router
  .route('/:userId')
  .get(auth, validate(userValidation.getUserById), userController.getUserById)
  .put(auth, validate(userValidation.updateUser), userController.updateUser)

router
  .route('/:userId/favorites')
  .get(auth, validate(userValidation.getFavoriteBooks), userController.getFavoriteBooks)

router
  .route('/:userId/favorites/:bookId')
  .post(auth, validate(userValidation.addFavoriteBook), userController.addFavoriteBook)
  .delete(auth, validate(userValidation.removeFavoriteBook), userController.removeFavoriteBook)

module.exports = router
