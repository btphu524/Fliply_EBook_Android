const userSocket = require('./userSocket')
const userModel = require('./userModel')

const initializeSockets = (io) => {
  io.use(async (socket, next) => {
    const userId = socket.handshake.auth.userId
    if (!userId) {
      return next(new Error('Invalid user ID'))
    }
    try {
      const user = await userModel.findById(userId)
      if (!user) {
        return next(new Error('User not found or not active'))
      }
      socket.userId = userId
      next()
    } catch (error) {
      return next(new Error('Invalid user: ' + error.message))
    }
  })

  userSocket(io)
}

module.exports = initializeSockets
