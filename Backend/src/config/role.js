const allRoles = {
  user: [],
  admin: ['getUsers', 'deleteUser',
    'createBook', 'updateBook', 'deleteBook',
    'createCategory', 'updateCategory', 'deleteCategory'
  ]
}
const roles = Object.keys(allRoles)
const roleRights = new Map(Object.entries(allRoles))

module.exports = {
  roles,
  roleRights
}
