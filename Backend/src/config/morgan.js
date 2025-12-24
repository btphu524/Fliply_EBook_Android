const morgan = require('morgan')
const logger = require('./logger')

morgan.token('message', (req, res) => res.locals.errorMessage || '')

morgan.token('client-ip', (req) => req.headers['x-forwarded-for'] || req.ip)

/**
 * Format for successful logs (status < 400)
 */
const successResponseFormat =
  ':client-ip - :method :url :status - :response-time ms'

/**
 * Format for error logs (status >= 400)
 */
const errorResponseFormat =
  ':client-ip - :method :url :status - :response-time ms - message: :message'

/**
 * Middleware for successful request logging
 */
const successHandler = morgan(successResponseFormat, {
  skip: (req, res) => res.statusCode >= 400,
  stream: { write: (message) => logger.info(message.trim()) }
})

/**
 * Middleware for error request logging
 */
const errorHandler = morgan(errorResponseFormat, {
  skip: (req, res) => res.statusCode < 400,
  stream: { write: (message) => logger.error(message.trim()) }
})

module.exports = {
  successHandler,
  errorHandler
}
