const http = require('http')
const app = require('./app')
const config = require('./config/config')
const logger = require('./config/logger')


const port = process.env.PORT || config.app.port || 3000
const prefix = config.app.prefix || ''
// Always use 0.0.0.0 to bind to all interfaces
const host = '0.0.0.0'

let server = null

const startServer = () => {
  try {
    // Log configuration before starting
    logger.info(`🔧 Server configuration: host=${host}, port=${port}, env=${process.env.NODE_ENV || config.env}`)
    logger.info(`🔧 Environment variables: APP_HOST=${process.env.APP_HOST}, HOST=${process.env.HOST}, NODE_ENV=${process.env.NODE_ENV}`)

    server = http.createServer(app)

    server.listen(port, host, () => {
      const address = server.address()
      let actualHost = address.address
      if (address.address === '::' || address.address === '0.0.0.0') {
        actualHost = '0.0.0.0'
      } else if (address.address === '127.0.0.1' || address.address === '::1') {
        actualHost = '127.0.0.1'
        logger.error(`❌ ERROR: Server bound to ${address.address} instead of 0.0.0.0! This will cause connectivity issues.`)
        logger.error(`❌ Attempted to bind to: ${host}:${port}`)
      }

      logger.info(`🚀 Server running at http://${actualHost}:${address.port}${prefix}`)
      logger.info(`📦 Environment: ${config.env}`)
      logger.info(`⏰ Started at: ${new Date().toISOString()}`)
    })

    // Handle server errors
    server.on('error', (error) => {
      if (error.code === 'EADDRINUSE') {
        logger.error(`❌ Port ${port} is already in use. Please choose a different port.`)
      } else {
        logger.error('❌ Server error:', error)
      }
      process.exit(1)
    })

  } catch (error) {
    logger.error('❌ Failed to start server:', error)
    process.exit(1)
  }
}

// GRACEFUL SHUTDOWN
const gracefulShutdown = (signal) => {
  logger.info(`📴 Received signal ${signal}. Shutting down server...`)

  if (server) {
    server.close((error) => {
      if (error) {
        logger.error('❌ Error shutting down server:', error)
        process.exit(1)
      } else {
        logger.info('✅ Server shut down successfully')
        process.exit(0)
      }
    })

    // Force close after 10 seconds
    setTimeout(() => {
      logger.error('Timeout! Force closing server...')
      process.exit(1)
    }, 10000)
  } else {
    process.exit(0)
  }
}

// ERROR HANDLERS
const handleUnexpectedError = (error, source) => {
  logger.error(`❌ ${source || 'Unexpected error'}:`, {
    message: error.message,
    stack: error.stack,
    name: error.name,
    ...(error.details && { details: error.details })
  })

  // Đợi một chút để log được ghi xong trước khi shutdown
  setTimeout(() => {
    gracefulShutdown(source || 'UNCAUGHT_EXCEPTION')
  }, 2000)
}

// PROCESS EVENT LISTENERS
process.on('uncaughtException', (error) => handleUnexpectedError(error, 'UNCAUGHT_EXCEPTION'))
process.on('unhandledRejection', (error) => handleUnexpectedError(error, 'UNHANDLED_REJECTION'))
process.on('SIGTERM', () => gracefulShutdown('SIGTERM'))
process.on('SIGINT', () => gracefulShutdown('SIGINT'))

startServer()
