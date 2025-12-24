const httpStatus = require('http-status')
const { ApiError, hashPassword } = require('../utils/index')
const { userModel } = require('../models/index')
const bookService = require('./bookService')
const admin = require('firebase-admin')

/**
 * Lấy thông tin người dùng theo ID
 * @param {Object} data - Dữ liệu yêu cầu
 * @param {string} data.id - ID người dùng
 * @returns {Promise<Object>} - Thông tin người dùng
 * @throws {ApiError} - Nếu không tìm thấy người dùng
 */
const getUserById = async (data) => {
  const { id } = data
  try {
    if (!id)
      throw new ApiError(
        httpStatus.BAD_REQUEST,
        'User ID is required'
      )
    const user = await userModel.findById(id)
    if (!user || !user.isActive) {
      throw new ApiError(httpStatus.NOT_FOUND, 'User not found')
    }
    return { _id: id, ...user }
  } catch (error) {
    if (error instanceof ApiError) throw error
    throw new ApiError(
      httpStatus.INTERNAL_SERVER_ERROR,
      `Failed to get user information: ${error.message}`
    )
  }
}

/**
 * Lấy thông tin người dùng theo email
 * @param {Object} data - Dữ liệu yêu cầu
 * @param {string} data.email - Email người dùng
 * @returns {Promise<Object>} - Thông tin người dùng
 * @throws {ApiError} - Nếu không tìm thấy người dùng
 */
const getUserByEmail = async (data) => {
  const { email } = data
  try {
    if (!email)
      throw new ApiError(httpStatus.BAD_REQUEST, 'Email is required')
    const user = await userModel.findByEmail(email.trim().toLowerCase())
    return user
  } catch (error) {
    if (error instanceof ApiError) throw error
    throw new ApiError(
      httpStatus.INTERNAL_SERVER_ERROR,
      `Failed to get user information: ${error.message}`
    )
  }
}

/**
 * Cập nhật thông tin người dùng theo ID
 * @param {Object} data - Dữ liệu yêu cầu
 * @param {string} data.userId - ID người dùng
 * @param {Object} data.updateBody - Dữ liệu cập nhật
 * @returns {Promise<Object>} - Thông tin người dùng đã cập nhật
 * @throws {ApiError} - Nếu cập nhật thất bại
 */
const updateUserById = async (data) => {
  const { userId, updateBody } = data
  try {
    const user = await getUserById({ id: userId })

    if (
      updateBody.email &&
      updateBody.email.trim().toLowerCase() !== user.email.trim().toLowerCase()
    ) {
      const users = await userModel.findByEmail(
        updateBody.email.trim().toLowerCase()
      )
      if (users && Object.keys(users).some((id) => id !== userId)) {
        throw new ApiError(httpStatus.BAD_REQUEST, 'Email already in use')
      }

      await admin
        .auth()
        .updateUser(userId, { email: updateBody.email.trim().toLowerCase() })
    }

    const hashedPassword = updateBody.password
      ? await hashPassword(updateBody.password)
      : user.password

    const updatedData = {
      email: updateBody.email
        ? updateBody.email.trim().toLowerCase()
        : user.email,
      phoneNumber: updateBody.phoneNumber
        ? updateBody.phoneNumber.trim()
        : user.phoneNumber,
      password: hashedPassword,
      fullName: updateBody.fullName
        ? updateBody.fullName.trim()
        : user.fullName,
      role: updateBody.role || user.role,
      avatar: updateBody.avatar ? updateBody.avatar.trim() : user.avatar,
      preferences: updateBody.preferences || user.preferences,
      comments: updateBody.comments || user.comments,
      history: updateBody.history || user.history,
      customId: user.customId,
      isActive: user.isActive,
      updatedAt: admin.database.ServerValue.TIMESTAMP
    }

    await userModel.update(userId, updatedData)
    const updatedUser = await userModel.findById(userId)
    return { _id: userId, ...updatedUser }
  } catch (error) {
    if (error instanceof ApiError) throw error
    throw new ApiError(
      httpStatus.INTERNAL_SERVER_ERROR,
      `Failed to update user: ${error.message}`
    )
  }
}


/**
 * Add book to user's favorites
 * @param {string} userId - User ID
 * @param {string} bookId - Book ID
 * @returns {Promise<Object>} - Success response
 * @throws {ApiError} 404 - User not found, 400 - Book already in favorites
 */
const addFavoriteBook = async (data) => {
  const { userId, bookId } = data
  try {
    if (!userId || !bookId) {
      throw new ApiError(
        httpStatus.BAD_REQUEST,
        'User ID and Book ID are required'
      )
    }

    await userModel.addFavoriteBook(userId, bookId)
    return {
      success: true,
      message: 'Book added to favorites successfully'
    }
  } catch (error) {
    if (error instanceof ApiError) throw error
    throw new ApiError(
      httpStatus.INTERNAL_SERVER_ERROR,
      `Failed to add favorite book: ${error.message}`
    )
  }
}

/**
 * Remove book from user's favorites
 * @param {string} userId - User ID
 * @param {string} bookId - Book ID
 * @returns {Promise<Object>} - Success response
 * @throws {ApiError} 404 - User not found, 400 - Book not in favorites
 */
const removeFavoriteBook = async (data) => {
  const { userId, bookId } = data
  try {
    if (!userId || !bookId) {
      throw new ApiError(
        httpStatus.BAD_REQUEST,
        'User ID and Book ID are required'
      )
    }

    await userModel.removeFavoriteBook(userId, bookId)
    return {
      success: true,
      message: 'Book removed from favorites successfully'
    }
  } catch (error) {
    if (error instanceof ApiError) throw error
    throw new ApiError(
      httpStatus.INTERNAL_SERVER_ERROR,
      `Failed to remove favorite book: ${error.message}`
    )
  }
}

/**
 * Get user's favorite books
 * @param {string} userId - User ID
 * @returns {Promise<Object>} - List of favorite books with details
 * @throws {ApiError} 404 - User not found
 */
const getFavoriteBooks = async (data) => {
  const { userId } = data
  try {
    if (!userId) {
      throw new ApiError(
        httpStatus.BAD_REQUEST,
        'User ID is required'
      )
    }

    const favoriteBookIds = await userModel.getFavoriteBooks(userId)
    const booksResult = await bookService.getFavoriteBooksDetails({ bookIds: favoriteBookIds })
    return {
      success: true,
      data: {
        favoriteBooks: booksResult.data.books
      }
    }
  } catch (error) {
    if (error instanceof ApiError) throw error
    throw new ApiError(
      httpStatus.INTERNAL_SERVER_ERROR,
      `Failed to get favorite books: ${error.message}`
    )
  }
}

module.exports = {
  getUserById,
  getUserByEmail,
  updateUserById,
  addFavoriteBook,
  removeFavoriteBook,
  getFavoriteBooks
}
