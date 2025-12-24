const db = require('../config/db.js')
const { ApiError } = require('../utils/index')
const { hashPassword } = require('../utils/passwordUtils')
const { generateCustomId } = require('../utils/idUtils')
const httpStatus = require('http-status')

const userModel = {
  /**
   * Tạo người dùng mới
   * @param {Object} userData - Dữ liệu người dùng
   * @returns {Promise<Object>} - ID người dùng và thông báo
   * @throws {ApiError} - Nếu dữ liệu không hợp lệ hoặc tạo thất bại
   */
  create: async (userData) => {
    try {
      // Kiểm tra dữ liệu đầu vào
      if (
        !userData.email ||
        !userData.password ||
        !userData.fullName ||
        !userData.phoneNumber ||
        !userData.confirmPassword
      ) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'Email, mật khẩu, xác nhận mật khẩu, họ tên và số điện thoại là bắt buộc'
        )
      }

      // Xử lý userId
      let userId = userData._id || userData.userId
      if (userId) {
        // Kiểm tra userId đã tồn tại chưa
        const existingUser = await db.getRef(`users/${userId}`).once('value')
        if (existingUser.val()) {
          throw new ApiError(
            httpStatus.status.BAD_REQUEST,
            'ID người dùng đã tồn tại'
          )
        }

        // Kiểm tra userId hợp lệ
        if (isNaN(userId) || userId <= 0) {
          throw new ApiError(
            httpStatus.status.BAD_REQUEST,
            'ID người dùng phải là số nguyên dương'
          )
        }

        userId = parseInt(userId)
      }

      // Kiểm tra mật khẩu và xác nhận mật khẩu
      if (userData.password !== userData.confirmPassword) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'Xác nhận mật khẩu không khớp'
        )
      }

      // Kiểm tra số điện thoại
      const phoneRegex = /^[0-9]{10,11}$/
      if (!phoneRegex.test(userData.phoneNumber.trim())) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'Số điện thoại không hợp lệ'
        )
      }

      // Chuẩn bị dữ liệu user
      const sanitizedData = {
        fullName: userData.fullName.trim(),
        email: userData.email.trim().toLowerCase(),
        phoneNumber: userData.phoneNumber.trim(),
        password: await hashPassword(userData.password),
        avatar: userData.avatar?.trim() || '',
        preferences: Array.isArray(userData.preferences)
          ? userData.preferences
          : [],
        isActive: false,
        isOnline: false,
        lastSeen: Date.now(),
        role: userData.role?.trim() || 'user',
        token: userData.token || null,
        comments: [],
        history: [],
        favoriteBooks: [],
        lastLogin: Date.now(),
        createdAt: Date.now(),
        updatedAt: Date.now()
      }

      // Tạo user trong database
      const userRef = db.getRef('users')
      let newUserRef

      if (userId) {
        newUserRef = userRef.child(userId)
      } else {
        const newCustomId = await generateCustomId()
        userId = parseInt(newCustomId)
        newUserRef = userRef.child(userId)
      }

      await newUserRef.set({ _id: userId, ...sanitizedData })

      return {
        userId,
        message: 'Người dùng đã được tạo thành công, vui lòng xác thực OTP'
      }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Tạo người dùng thất bại: ${error.message}`
        )
    }
  },

  /**
   * Tìm người dùng theo ID
   * @param {string} userId - ID người dùng
   * @returns {Promise<Object>} - Đối tượng người dùng
   * @throws {ApiError} - Nếu không tìm thấy hoặc chưa kích hoạt
   */
  findById: async (userId) => {
    try {
      // Kiểm tra userId
      if (!userId) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ID người dùng là bắt buộc'
        )
      }

      // Tìm user theo ID
      const snapshot = await db.getRef(`users/${userId}`).once('value')
      const user = snapshot.val()
      if (!user || !user.isActive) {
        throw new ApiError(
          httpStatus.status.NOT_FOUND,
          'Không tìm thấy người dùng hoặc chưa được kích hoạt'
        )
      }
      return { _id: userId, ...user }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Lấy thông tin người dùng thất bại: ${error.message}`
        )
    }
  },

  /**
   * Tìm người dùng theo email
   * @param {string} email - Email người dùng
   * @returns {Promise<Object>} - Đối tượng người dùng
   * @throws {ApiError} - Nếu không tìm thấy hoặc chưa kích hoạt
   */
  findByEmail: async (email) => {
    try {
      // Kiểm tra email
      if (!email || typeof email !== 'string') {
        throw new ApiError(httpStatus.status.BAD_REQUEST, 'Email là bắt buộc')
      }

      // Tìm user theo email
      const snapshot = await db
        .getRef('users')
        .orderByChild('email')
        .equalTo(email.trim().toLowerCase())
        .once('value')
      const users = snapshot.val()
      if (!users) {
        throw new ApiError(httpStatus.status.NOT_FOUND, 'User not found or not activated')
      }

      const userId = Object.keys(users)[0]
      const user = users[userId]
      if (!user.isActive) {
        throw new ApiError(
          httpStatus.status.NOT_FOUND,
          'User not found or not activated'
        )
      }
      return { _id: userId, ...user }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Lấy thông tin người dùng thất bại: ${error.message}`
        )
    }
  },

  /**
   * Tìm người dùng theo email (không kiểm tra isActive)
   * @param {string} email - Email người dùng
   * @returns {Promise<Object>} - Đối tượng người dùng
   * @throws {ApiError} 404 - Không tìm thấy người dùng
   */
  findByEmailForActivation: async (email) => {
    try {
      // Kiểm tra email
      if (!email || typeof email !== 'string') {
        throw new ApiError(httpStatus.status.BAD_REQUEST, 'Email là bắt buộc')
      }

      // Tìm user theo email (không kiểm tra isActive)
      const snapshot = await db
        .getRef('users')
        .orderByChild('email')
        .equalTo(email.trim().toLowerCase())
        .once('value')
      const users = snapshot.val()
      if (!users) {
        throw new ApiError(httpStatus.status.NOT_FOUND, 'Không tìm thấy người dùng')
      }

      const userId = Object.keys(users)[0]
      const user = users[userId]
      return { _id: userId, ...user }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Lấy thông tin người dùng thất bại: ${error.message}`
        )
    }
  },

  /**
   * Cập nhật thông tin người dùng
   * @param {string} userId - ID người dùng
   * @param {Object} updateData - Dữ liệu cập nhật
   * @returns {Promise<boolean>} - Trạng thái cập nhật
   * @throws {ApiError} - Nếu cập nhật thất bại
   */
  update: async (userId, updateData) => {
    try {
      // Kiểm tra userId
      if (!userId) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ID người dùng là bắt buộc'
        )
      }

      // Chuẩn bị dữ liệu cập nhật
      const sanitizedUpdateData = {
        updatedAt: Date.now()
      }

      // Xử lý các trường cập nhật
      if (updateData.email) {
        sanitizedUpdateData.email = updateData.email.trim().toLowerCase()
      }
      if (updateData.fullName) {
        sanitizedUpdateData.fullName = updateData.fullName.trim()
      }
      if (updateData.phoneNumber) {
        sanitizedUpdateData.phoneNumber = updateData.phoneNumber.trim()
      }
      if (updateData.avatar) {
        sanitizedUpdateData.avatar = updateData.avatar.trim()
      }
      if (updateData.isOnline !== undefined) {
        sanitizedUpdateData.isOnline = updateData.isOnline
      }
      if (updateData.lastSeen) {
        sanitizedUpdateData.lastSeen = updateData.lastSeen
      }
      if (updateData.lastLogin) {
        sanitizedUpdateData.lastLogin = updateData.lastLogin
      }

      // Kiểm tra số điện thoại nếu có
      if (sanitizedUpdateData.phoneNumber) {
        const phoneRegex = /^[0-9]{10,11}$/
        if (!phoneRegex.test(sanitizedUpdateData.phoneNumber)) {
          throw new ApiError(
            httpStatus.status.BAD_REQUEST,
            'Số điện thoại không hợp lệ'
          )
        }
      }

      // Cập nhật user trong database
      await db.getRef(`users/${userId}`).update(sanitizedUpdateData)
      return true
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Cập nhật người dùng thất bại: ${error.message}`
        )
    }
  },

  /**
   * Cập nhật mật khẩu người dùng
   * @param {string} email - Email người dùng
   * @param {string} newPassword - Mật khẩu mới
   * @returns {Promise<boolean>} - Trạng thái cập nhật
   * @throws {ApiError} - Nếu cập nhật thất bại
   */
  updatePassword: async (email, newPassword) => {
    try {
      // Kiểm tra dữ liệu đầu vào
      if (!email || !newPassword) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'Email và mật khẩu mới là bắt buộc'
        )
      }

      // Tìm user và hash password mới
      const user = await userModel.findByEmail(email)
      const userId = user._id
      const hashedPassword = await hashPassword(newPassword)

      // Cập nhật password trong database
      await db.getRef(`users/${userId}`).update({
        password: hashedPassword,
        updatedAt: Date.now()
      })

      return true
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Cập nhật mật khẩu thất bại: ${error.message}`
        )
    }
  },

  /**
   * Kích hoạt người dùng sau khi xác thực OTP
   * @param {string} userId - ID người dùng
   * @returns {Promise<boolean>} - Trạng thái kích hoạt
   * @throws {ApiError} - Nếu kích hoạt thất bại
   */
  activateUser: async (userId) => {
    try {
      // Kiểm tra userId
      if (!userId) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ID người dùng là bắt buộc'
        )
      }

      // Kích hoạt user
      await db.getRef(`users/${userId}`).update({
        isActive: true,
        updatedAt: Date.now()
      })
      return true
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Kích hoạt người dùng thất bại: ${error.message}`
        )
    }
  },

  /**
   * Thêm sách vào danh sách yêu thích
   * @param {string} userId - ID người dùng
   * @param {string} bookId - ID sách
   * @returns {Promise<boolean>} - Trạng thái thêm
   * @throws {ApiError} - Nếu thêm thất bại
   */
  addFavoriteBook: async (userId, bookId) => {
    try {
      if (!userId || !bookId) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ID người dùng và ID sách là bắt buộc'
        )
      }

      const user = await userModel.findById(userId)
      if (!user) {
        throw new ApiError(
          httpStatus.status.NOT_FOUND,
          'Không tìm thấy người dùng'
        )
      }

      const bookModel = require('./bookModel')
      const book = await bookModel.getById(bookId)
      if (!book) {
        throw new ApiError(
          httpStatus.status.NOT_FOUND,
          'Không tìm thấy sách trong hệ thống'
        )
      }

      if (user.favoriteBooks && user.favoriteBooks.includes(bookId)) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'Sách đã có trong danh sách yêu thích'
        )
      }

      const currentFavorites = user.favoriteBooks || []
      const updatedFavorites = [...currentFavorites, bookId]

      await db.getRef(`users/${userId}`).update({
        favoriteBooks: updatedFavorites,
        updatedAt: Date.now()
      })

      return true
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Thêm sách yêu thích thất bại: ${error.message}`
        )
    }
  },

  /**
   * Xóa sách khỏi danh sách yêu thích
   * @param {string} userId - ID người dùng
   * @param {string} bookId - ID sách
   * @returns {Promise<boolean>} - Trạng thái xóa
   * @throws {ApiError} - Nếu xóa thất bại
   */
  removeFavoriteBook: async (userId, bookId) => {
    try {
      if (!userId || !bookId) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ID người dùng và ID sách là bắt buộc'
        )
      }

      const user = await userModel.findById(userId)
      if (!user) {
        throw new ApiError(
          httpStatus.status.NOT_FOUND,
          'Không tìm thấy người dùng'
        )
      }

      const bookModel = require('./bookModel')
      const book = await bookModel.getById(bookId)
      if (!book) {
        throw new ApiError(
          httpStatus.status.NOT_FOUND,
          'Không tìm thấy sách trong hệ thống'
        )
      }

      const currentFavorites = user.favoriteBooks || []
      if (!currentFavorites.includes(bookId)) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'Sách không có trong danh sách yêu thích'
        )
      }

      const updatedFavorites = currentFavorites.filter(id => id !== bookId)

      await db.getRef(`users/${userId}`).update({
        favoriteBooks: updatedFavorites,
        updatedAt: Date.now()
      })

      return true
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Xóa sách yêu thích thất bại: ${error.message}`
        )
    }
  },

  /**
   * Lấy danh sách sách yêu thích của người dùng
   * @param {string} userId - ID người dùng
   * @returns {Promise<Array>} - Danh sách ID sách yêu thích
   * @throws {ApiError} - Nếu lấy thất bại
   */
  getFavoriteBooks: async (userId) => {
    try {
      // Kiểm tra userId
      if (!userId) {
        throw new ApiError(
          httpStatus.status.BAD_REQUEST,
          'ID người dùng là bắt buộc'
        )
      }

      // Lấy thông tin user
      const user = await userModel.findById(userId)
      if (!user) {
        throw new ApiError(
          httpStatus.status.NOT_FOUND,
          'Không tìm thấy người dùng'
        )
      }

      return user.favoriteBooks || []
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.status.INTERNAL_SERVER_ERROR,
          `Lấy danh sách sách yêu thích thất bại: ${error.message}`
        )
    }
  }
}

module.exports = {
  create: userModel.create,
  findById: userModel.findById,
  findByEmail: userModel.findByEmail,
  findByEmailForActivation: userModel.findByEmailForActivation,
  update: userModel.update,
  updatePassword: userModel.updatePassword,
  activateUser: userModel.activateUser,
  addFavoriteBook: userModel.addFavoriteBook,
  removeFavoriteBook: userModel.removeFavoriteBook,
  getFavoriteBooks: userModel.getFavoriteBooks
}
