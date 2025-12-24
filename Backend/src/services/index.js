const userService = require('./userService')
const otpService = require('./otpService')
const tokenService = require('./tokenService')
const firebaseService = require('./firebaseService')
const emailService = require('./emailService')
const authService = require('./authService')
const bookService = require('./bookService')
const categoriesService = require('./categoriesService')
const epubService = require('./epubService')
const historyService = require('./historyService')
const feedbackService = require('./feedbackService')

module.exports = {
  userService,
  otpService,
  tokenService,
  firebaseService,
  emailService,
  authService,
  bookService,
  categoriesService,
  epubService,
  historyService,
  feedbackService
}
