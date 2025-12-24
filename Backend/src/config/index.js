const config = require('./config')
const logger = require('./logger')
const db = require('./db')
const morgan = require('./morgan')
const passport = require('./passport')
const { firebaseStrategy } = require('./passport')

module.exports = {
  config,
  logger,
  db,
  morgan,
  passport: {
    ...passport,
    firebaseStrategy
  }
}
