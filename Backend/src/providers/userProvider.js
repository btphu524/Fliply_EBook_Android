const { getRef } = require('../config/db')
const logger = require('../config/logger')

const ref = getRef('users')

/**
 * Listen to entire user object changes
 * @param {string} userId
 * @param {Function} callback
 */
function listenToUserChanges(userId, callback) {
  try {
    ref.child(userId).on('value', (snapshot) => {
      callback(snapshot.val())
    })
  } catch (error) {
    logger.error(`Error listening to user changes ${userId}: ${error.stack}`)
    throw error
  }
}

/**
 * Listen to online/offline status
 * @param {string} userId
 * @param {Function} callback
 */
function listenToUserStatus(userId, callback) {
  try {
    ref.child(`${userId}/isOnline`).on('value', (snapshot) => {
      callback(snapshot.val())
    })
  } catch (error) {
    logger.error(`Error listening to user status ${userId}: ${error.stack}`)
    throw error
  }
}

/**
 * Stop listening to user changes
 * @param {string} userId
 */
function stopListening(userId) {
  try {
    ref.child(userId).off()
  } catch (error) {
    logger.error(`Error stopping listener for user ${userId}: ${error.stack}`)
    throw error
  }
}

/**
 * Update user status
 * @param {string} userId
 * @param {boolean} isOnline
 */
async function updateUserStatus(userId, isOnline) {
  try {
    await ref.child(userId).update({
      isOnline,
      lastSeen: Date.now(),
      updatedAt: Date.now()
    })
  } catch (error) {
    logger.error(`Error updating user status ${userId}: ${error.stack}`)
    throw error
  }
}

module.exports = {
  listenToUserChanges,
  listenToUserStatus,
  stopListening,
  updateUserStatus
}
