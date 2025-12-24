const db = require('../config/db')
const { ApiError } = require('../utils/index')
const httpStatus = require('http-status')

const categoryModel = {
  /**
   * Lấy ID hiện tại của category và tạo ID mới
   * @returns {Promise<number>} - ID mới cho category
   */
  getNextCategoryId: async () => {
    try {
      const snapshot = await db.getRef('categories').once('value')
      const categories = snapshot.val()

      if (!categories || Object.keys(categories).length === 0) {
        // Nếu chưa có category nào, bắt đầu từ 1
        return 1
      }

      // Tìm ID lớn nhất
      let maxId = 0
      Object.keys(categories).forEach(key => {
        const id = parseInt(key, 10)
        if (!isNaN(id) && id > maxId) {
          maxId = id
        }
      })

      return maxId + 1
    } catch (error) {
      throw new ApiError(
        httpStatus.INTERNAL_SERVER_ERROR,
        `Lỗi khi tạo ID mới: ${error.message}`
      )
    }
  },

  /**
   * Lấy ID hiện tại lớn nhất của category (không tạo mới)
   * @returns {Promise<number>} - ID hiện tại lớn nhất
   */
  getCurrentMaxCategoryId: async () => {
    try {
      // Lấy tất cả categories để tìm ID lớn nhất
      const snapshot = await db.getRef('categories').once('value')
      const categories = snapshot.val()

      if (!categories || Object.keys(categories).length === 0) {
        return 0
      }

      let maxId = 0
      Object.keys(categories).forEach(key => {
        const id = parseInt(key, 10)
        if (!isNaN(id) && id > maxId) {
          maxId = id
        }
      })

      return maxId
    } catch (error) {
      throw new ApiError(
        httpStatus.INTERNAL_SERVER_ERROR,
        `Lỗi khi lấy ID hiện tại: ${error.message}`
      )
    }
  },

  /**
   * Tạo thể loại mới
   * @param {Object} categoryData - Dữ liệu thể loại
   * @returns {Promise<Object>} - ID thể loại và thông báo
   */
  create: async (categoryData) => {
    try {
      if (!categoryData.name || !categoryData.image_url) {
        throw new ApiError(
          httpStatus.BAD_REQUEST,
          'Tên thể loại và ảnh là bắt buộc'
        )
      }

      const existingCategory = await categoryModel.findByName(categoryData.name)
      if (existingCategory) {
        throw new ApiError(
          httpStatus.CONFLICT,
          'Thể loại với tên này đã tồn tại'
        )
      }

      const categoryId = await categoryModel.getNextCategoryId()

      const newCategory = {
        _id: categoryId,
        name: categoryData.name.trim(),
        image_url: categoryData.image_url.trim(),
        status: 'active',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }

      await db.getRef(`categories/${categoryId}`).set(newCategory)

      return {
        categoryId,
        message: 'Thể loại đã được tạo thành công'
      }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.INTERNAL_SERVER_ERROR,
          `Tạo thể loại thất bại: ${error.message}`
        )
    }
  },

  /**
   * Lấy tất cả thể loại
   * @returns {Promise<Object>} - Đối tượng thể loại
   */
  findAll: async () => {
    try {
      const snapshot = await db.getRef('categories').once('value')
      const categories = snapshot.val()
      return categories || {}
    } catch (error) {
      throw new ApiError(
        httpStatus.INTERNAL_SERVER_ERROR,
        `Lấy tất cả thể loại thất bại: ${error.message}`
      )
    }
  },

  /**
   * Lấy thể loại theo ID
   * @param {string} categoryId - ID thể loại
   * @returns {Promise<Object>} - Đối tượng thể loại
   */
  findById: async (categoryId) => {
    try {
      if (!categoryId) {
        throw new ApiError(
          httpStatus.BAD_REQUEST,
          'ID thể loại là bắt buộc'
        )
      }

      const snapshot = await db.getRef(`categories/${categoryId}`).once('value')
      const category = snapshot.val()

      if (!category) {
        throw new ApiError(
          httpStatus.NOT_FOUND,
          'Không tìm thấy thể loại'
        )
      }

      return { _id: categoryId, ...category }
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.INTERNAL_SERVER_ERROR,
          `Lấy thể loại thất bại: ${error.message}`
        )
    }
  },

  /**
   * Lấy thể loại theo tên
   * @param {string} name - Tên thể loại
   * @returns {Promise<Object|null>} - Đối tượng thể loại hoặc null
   */
  findByName: async (name) => {
    try {
      if (!name) {
        throw new ApiError(
          httpStatus.BAD_REQUEST,
          'Tên thể loại là bắt buộc'
        )
      }

      const snapshot = await db.getRef('categories').once('value')
      const categories = snapshot.val()

      if (!categories) {
        return null
      }

      for (const [id, category] of Object.entries(categories)) {
        if (category.name && category.name.toLowerCase() === name.trim().toLowerCase()) {
          return { _id: parseInt(id), ...category }
        }
      }

      return null
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.INTERNAL_SERVER_ERROR,
          `Lấy thể loại theo tên thất bại: ${error.message}`
        )
    }
  },

  /**
   * Cập nhật thể loại
   * @param {string} categoryId - ID thể loại
   * @param {Object} updateData - Dữ liệu cập nhật
   * @returns {Promise<boolean>} - Trạng thái cập nhật
   */
  update: async (categoryId, updateData) => {
    try {
      if (!categoryId) {
        throw new ApiError(
          httpStatus.BAD_REQUEST,
          'ID thể loại là bắt buộc'
        )
      }

      await categoryModel.findById(categoryId)

      if (updateData.name) {
        const categoryWithSameName = await categoryModel.findByName(updateData.name)
        if (categoryWithSameName && categoryWithSameName._id !== parseInt(categoryId)) {
          throw new ApiError(
            httpStatus.CONFLICT,
            'Thể loại với tên này đã tồn tại'
          )
        }
      }

      const sanitizedUpdateData = {
        updatedAt: new Date().toISOString()
      }

      if (updateData.name) {
        sanitizedUpdateData.name = updateData.name.trim()
      }
      if (updateData.image_url) {
        sanitizedUpdateData.image_url = updateData.image_url.trim()
      }
      if (updateData.status) {
        sanitizedUpdateData.status = updateData.status
      }

      await db.getRef(`categories/${categoryId}`).update(sanitizedUpdateData)
      return true
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.INTERNAL_SERVER_ERROR,
          `Cập nhật thể loại thất bại: ${error.message}`
        )
    }
  },

  /**
   * Xóa thể loại
   * @param {string} categoryId - ID thể loại
   * @returns {Promise<boolean>} - Trạng thái xóa
   */
  delete: async (categoryId) => {
    try {
      if (!categoryId) {
        throw new ApiError(
          httpStatus.BAD_REQUEST,
          'ID thể loại là bắt buộc'
        )
      }

      await categoryModel.findById(categoryId)

      await db.getRef(`categories/${categoryId}`).remove()
      return true
    } catch (error) {
      throw error instanceof ApiError
        ? error
        : new ApiError(
          httpStatus.INTERNAL_SERVER_ERROR,
          `Xóa thể loại thất bại: ${error.message}`
        )
    }
  }
}

module.exports = categoryModel
