const { userService, firebaseService } = require('../../src/services/index')
const { userModel } = require('../../src/models/index')
const httpStatus = require('http-status')
const { ApiError } = require('../../src/utils/index')

/**
 * Loại bỏ các trường hệ thống không được phép cập nhật
 */
const filterSystemFields = (data) => {
  const filtered = { ...data }
  const systemFields = ['_id', 'id', 'userId', 'createdAt', 'updatedAt', 'deletedAt', 'customId']
  systemFields.forEach(field => {
    delete filtered[field]
  })
  return filtered
}

/**
 * Xóa vĩnh viễn user khỏi database (hard delete)
 * @param {Object} data - Dữ liệu yêu cầu
 * @param {string} data.userId - ID của user
 * @returns {Promise<Object>} Kết quả xóa
 */
const hardDeleteUserById = async (data) => {
  const { userId } = data
  try {
    await userModel.hardDelete(userId)
    return {
      success: true,
      message: 'Xóa user vĩnh viễn thành công'
    }
  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  }
}

/**
 * Khôi phục user đã bị xóa mềm
 * @param {Object} data - Dữ liệu yêu cầu
 * @param {string} data.userId - ID của user
 * @returns {Promise<Object>} Kết quả khôi phục
 */
const restoreUserById = async (data) => {
  const { userId } = data
  try {
    await userModel.restore(userId)
    return {
      success: true,
      message: 'Khôi phục user thành công'
    }
  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  }
}

/**
 * Lấy danh sách users đã bị xóa mềm
 * @param {Object} data - Dữ liệu yêu cầu
 * @param {Object} data.options - Tùy chọn phân trang
 * @returns {Promise<Object>} Danh sách users đã xóa
 */
const getDeletedUsers = async (data) => {
  const { options = {} } = data
  try {
    const result = await userModel.getDeletedUsers(options)
    return {
      success: true,
      data: result
    }
  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  }
}

/**
 * Lấy thống kê users (admin only)
 * @returns {Promise<Object>} Thống kê users
 */
const getUserStats = async () => {
  try {
    const allUsers = await userModel.getAll()
    const deletedUsers = await userModel.getDeletedUsers({})

    const stats = {
      totalUsers: allUsers.length,
      activeUsers: allUsers.length,
      deletedUsers: deletedUsers.users?.length || 0,
      newUsersThisMonth: allUsers.filter(user => {
        const userDate = new Date(user.createdAt)
        const now = new Date()
        return userDate.getMonth() === now.getMonth() && userDate.getFullYear() === now.getFullYear()
      }).length
    }

    return {
      success: true,
      data: stats
    }
  } catch (error) {
    return {
      success: false,
      message: error.message
    }
  }
}

module.exports = {
  // Re-export các service từ src
  ...userService,

  // Admin-specific services
  /**
   * Loại bỏ các trường hệ thống không được phép cập nhật
   */
  filterSystemFields,

  /**
   * Tạo user mới (admin tạo)
   * @param {Object} data
   * @returns {Promise<Object>}
   */
  createUser: async (data) => {
    try {
      // Filter system fields trước khi xử lý
      const filteredData = filterSystemFields(data)
      const { email, password } = filteredData || {}
      if (!email || !password) {
        throw new ApiError(httpStatus.status.BAD_REQUEST, 'Email và mật khẩu là bắt buộc')
      }

      await firebaseService.createAuthUser({ email, password })
      const { userId, message } = await userModel.create(filteredData)

      return {
        success: true,
        message,
        data: { userId }
      }
    } catch (error) {
      return {
        success: false,
        message: error.message
      }
    }
  },

  /**
   * Xóa mềm user (đánh dấu isActive=false, lưu deletedAt)
   * @param {Object} data
   * @param {string|number} data.userId
   * @returns {Promise<Object>}
   */
  deleteUserById: async (data) => {
    const { userId } = data || {}
    try {
      if (!userId) {
        throw new ApiError(httpStatus.status.BAD_REQUEST, 'ID user là bắt buộc')
      }
      await userModel.update(userId, { isActive: false, deletedAt: Date.now() })
      return {
        success: true,
        message: 'Xóa mềm user thành công'
      }
    } catch (error) {
      return {
        success: false,
        message: error.message
      }
    }
  },

  hardDeleteUserById,
  restoreUserById,
  getDeletedUsers,
  getUserStats
}
