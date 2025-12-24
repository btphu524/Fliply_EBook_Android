const { bookModel } = require('../../src/models/index')

const filterSystemFields = (data) => {
  const filtered = { ...data }
  const systemFields = ['_id', 'id', 'createdAt', 'updatedAt', 'deletedAt']
  systemFields.forEach(field => {
    delete filtered[field]
  })
  return filtered
}

/**
 * Tạo sách mới
 */
const createBook = async ({ bookData }) => {
  try {
    const filteredData = filterSystemFields(bookData)
    const created = await bookModel.create(filteredData)
    return { success: true, data: { book: created }, message: 'Tạo sách thành công' }
  } catch (error) {
    return { success: false, message: error.message }
  }
}

/**
 * Cập nhật sách theo ID
 */
const updateBookById = async ({ id, updateData }) => {
  try {
    const filteredData = filterSystemFields(updateData)
    const updated = await bookModel.update(id, filteredData)
    return { success: true, data: { book: updated }, message: 'Cập nhật sách thành công' }
  } catch (error) {
    return { success: false, message: error.message }
  }
}

/**
 * Xóa mềm sách
 */
const deleteBookById = async ({ id }) => {
  try {
    await bookModel.delete(id)
    return { success: true, message: 'Xóa mềm sách thành công' }
  } catch (error) {
    return { success: false, message: error.message }
  }
}

/**
 * Xóa vĩnh viễn sách
 */
const hardDeleteBookById = async ({ id }) => {
  try {
    if (typeof bookModel.hardDelete === 'function') {
      await bookModel.hardDelete(id)
    } else {
      // Nếu không có hardDelete, có thể xóa trực tiếp
      await bookModel.delete(id)
    }
    return { success: true, message: 'Xóa vĩnh viễn sách thành công' }
  } catch (error) {
    return { success: false, message: error.message }
  }
}

/**
 * Khôi phục sách đã xóa mềm
 */
const restoreBookById = async ({ id }) => {
  try {
    if (typeof bookModel.restore === 'function') {
      await bookModel.restore(id)
    } else {
      await bookModel.update(id, { status: 'active', deletedAt: null })
    }
    const book = await bookModel.getById(id)
    return { success: true, data: { book }, message: 'Khôi phục sách thành công' }
  } catch (error) {
    return { success: false, message: error.message }
  }
}

/**
 * Lấy danh sách sách đã xóa mềm
 */
const getDeletedBooks = async ({ options = {} } = {}) => {
  try {
    const all = await bookModel.getAll()
    const items = all.filter(b => b.status !== 'active' || b.deletedAt)
    return { success: true, data: { books: items }, message: 'Lấy danh sách sách đã xóa mềm thành công' }
  } catch (error) {
    return { success: false, message: error.message }
  }
}

module.exports = {
  createBook,
  updateBookById,
  deleteBookById,
  hardDeleteBookById,
  restoreBookById,
  getDeletedBooks
}
