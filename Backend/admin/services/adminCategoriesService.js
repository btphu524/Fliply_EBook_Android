const { categoryModel } = require('../../src/models/index')
const httpStatus = require('http-status')
const { ApiError } = require('../../src/utils/index')

const filterSystemFields = (data) => {
  const filtered = { ...data }
  const systemFields = ['_id', 'id', 'createdAt', 'updatedAt', 'deletedAt']
  systemFields.forEach(field => {
    delete filtered[field]
  })
  return filtered
}

/**
 * Tạo category mới
 */
const createCategory = async (data) => {
  try {
    const filteredData = filterSystemFields(data)
    const { categoryId } = await categoryModel.create(filteredData)
    return { success: true, data: { categoryId }, message: 'Tạo thể loại thành công' }
  } catch (error) {
    return { success: false, message: error.message }
  }
}

/**
 * Cập nhật category
 */
const updateCategory = async (categoryId, updateData) => {
  try {
    const filteredData = filterSystemFields(updateData)
    await categoryModel.update(categoryId, filteredData)
    const updated = await categoryModel.findById(categoryId)
    return { success: true, data: { category: updated }, message: 'Cập nhật thể loại thành công' }
  } catch (error) {
    return { success: false, message: error.message }
  }
}

/**
 * Xóa mềm category (đánh dấu inactive)
 */
const deleteCategory = async (categoryId) => {
  try {
    if (!categoryId) throw new ApiError(httpStatus.status.BAD_REQUEST, 'ID thể loại là bắt buộc')
    await categoryModel.update(categoryId, { status: 'inactive', deletedAt: new Date().toISOString() })
    return { success: true, message: 'Xóa mềm thể loại thành công' }
  } catch (error) {
    return { success: false, message: error.message }
  }
}

/**
 * Xóa vĩnh viễn category
 */
const hardDeleteCategory = async (categoryId) => {
  try {
    await categoryModel.delete(categoryId)
    return { success: true, message: 'Xóa vĩnh viễn thể loại thành công' }
  } catch (error) {
    return { success: false, message: error.message }
  }
}

/**
 * Khôi phục category đã xóa mềm
 */
const restoreCategory = async (categoryId) => {
  try {
    await categoryModel.update(categoryId, { status: 'active', deletedAt: null })
    const category = await categoryModel.findById(categoryId)
    return { success: true, data: { category }, message: 'Khôi phục thể loại thành công' }
  } catch (error) {
    return { success: false, message: error.message }
  }
}

/**
 * Lấy danh sách category đã xóa mềm
 */
const getDeletedCategories = async ({ options = {} } = {}) => {
  try {
    const all = await categoryModel.findAll()
    const items = []
    if (all) {
      for (const [id, cat] of Object.entries(all)) {
        if (cat.status !== 'active' || cat.deletedAt) {
          items.push({ _id: parseInt(id), ...cat })
        }
      }
    }
    return { success: true, data: { categories: items }, message: 'Lấy danh sách thể loại đã xóa mềm thành công' }
  } catch (error) {
    return { success: false, message: error.message }
  }
}

module.exports = {
  createCategory,
  updateCategory,
  deleteCategory,
  hardDeleteCategory,
  restoreCategory,
  getDeletedCategories
}
