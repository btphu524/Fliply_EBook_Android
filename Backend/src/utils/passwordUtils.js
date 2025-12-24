const bcrypt = require('bcrypt')

const hashPassword = async (password) => {
  const saltRounds = 10
  return await bcrypt.hash(password, saltRounds)
}

const comparePassword = async (plainPassword, hashedPassword) => {
  return await bcrypt.compare(plainPassword, hashedPassword)
}

module.exports = { hashPassword, comparePassword }
