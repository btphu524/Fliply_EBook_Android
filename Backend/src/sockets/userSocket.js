const { userProvider } = require('../providers/index')
const logger = require('../config/logger')

const userSocket = (io) => {
  const userNamespace = io.of('/users')

  userNamespace.on('connection', (socket) => {
    const userId = socket.userId

    socket.on('user:connect', async () => {
      socket.join(`user:${userId}`)
      await userProvider.updateUserStatus(userId, true)
      logger.info(`User ${userId} connected to socket`)
    })

    userProvider.listenToUserChanges(userId, (userData) => {
      socket.emit('user:updated', userData)
      logger.debug(`User ${userId} data updated: ${JSON.stringify(userData)}`)
    })

    socket.on('user:otpSent', (data) => {
      socket.emit('user:otpNotification', {
        message: `OTP sent to ${data.email} for ${data.type}`,
        email: data.email,
        type: data.type
      })
      logger.info(`OTP notification sent to user ${userId} for ${data.email}`)
    })

    socket.on('user:profileUpdated', (data) => {
      socket.to(`user:${userId}`).emit('user:profileUpdated', {
        userId,
        updatedFields: data.updatedFields
      })
      logger.info(`Profile update notification sent for user ${userId}`)
    })

    socket.on('disconnect', async () => {
      socket.leave(`user:${userId}`)
      await userProvider.updateUserStatus(userId, false)
      await userProvider.stopListening(userId)
      logger.info(`User ${userId} disconnected from socket`)
    })

    socket.on('user:typing', (data) => {
      socket.to(`user:${data.targetUserId}`).emit('user:typing', {
        userId: userId,
        isTyping: data.isTyping
      })
      logger.debug(`Typing event from user ${userId} to ${data.targetUserId}`)
    })
  })
}

module.exports = userSocket
