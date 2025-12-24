const express = require('express')
const cors = require('cors')
const passport = require('passport')
const rateLimit = require('express-rate-limit')
const compression = require('compression')
const helmet = require('helmet')
const httpStatus = require('http-status')

const config = require('./config/config')
const logger = require('./config/logger')
const { successHandler, errorHandler } = require('./config/morgan')
const { authRoute, userRoute, categoriesRoute, bookRoute, epubRoute, historyRoute, feedbackRoute } = require('./routes/index')
const adminRoutes = require('../admin/routes/index')
const { firebaseStrategy } = require('./config/passport')

const app = express()

// SECURITY MIDDLEWARE
app.use(helmet())
app.use(compression())

// CORS CONFIGURATION
app.use(cors(config.cors))
app.options('*', cors())

// BODY PARSING
app.use(
  express.json({
    limit: config.upload.limit || '10mb',
    verify: (req, res, buf) => {
      req.rawBody = buf
    }
  })
)

// Handle JSON parsing for requests without proper Content-Type
app.use((req, res, next) => {
  // Handle text/plain content type that might contain JSON
  if (req.is('text/plain') && req.body && typeof req.body === 'string') {
    try {
      req.body = JSON.parse(req.body)
    } catch (e) {
      // If parsing fails, continue with original body
    }
  }

  // Handle requests with no content type that might contain JSON
  if (!req.get('Content-Type') && req.body && typeof req.body === 'string') {
    try {
      req.body = JSON.parse(req.body)
    } catch (e) {
      // If parsing fails, continue with original body
    }
  }

  next()
})
app.use(
  express.urlencoded({
    extended: true,
    limit: config.upload.limit || '10mb'
  })
)

// REQUEST LOGGING
app.use(successHandler)
app.use(errorHandler)

// AUTHENTICATION
app.use(passport.initialize())
passport.use('firebase', firebaseStrategy)

// ROOT ENDPOINT
app.get('/', (req, res) => {
  res.status(200).json({
    success: true,
    message: '📚 Reading Book API - Flipy',
    version: '1.0.0',
    status: 'running',
    endpoints: {
      health: '/health',
      api: '/api',
      docs: 'https://github.com/HoangTung121/Project1'
    },
    timestamp: new Date().toISOString()
  })
})

// HEALTH CHECK (before rate limiting to ensure availability)
app.get('/health', (req, res) => {
  try {
    res.status(200).json({
      success: true,
      message: 'Server is running normally',
      timestamp: new Date().toISOString(),
      uptime: process.uptime(),
      environment: config.env || 'unknown'
    })
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Health check failed',
      error: error.message
    })
  }
})

// API HEALTH CHECK
app.get('/api/health', (req, res) => {
  try {
    res.status(200).json({
      success: true,
      message: 'Server is running normally',
      timestamp: new Date().toISOString(),
      uptime: process.uptime(),
      environment: config.env || 'unknown'
    })
  } catch (error) {
    res.status(500).json({
      success: false,
      message: 'Health check failed',
      error: error.message
    })
  }
})

// RATE LIMITING (applied after health checks)
const limiter = rateLimit({
  windowMs: config.rateLimit.windowMs,
  max: config.rateLimit.max,
  message: {
    success: false,
    message: 'Too many requests from this IP, please try again later'
  },
  standardHeaders: true,
  legacyHeaders: false,
  // Skip rate limiting for health checks
  skip: (req) => req.path === '/health' || req.path === '/api/health'
})
app.use(limiter)

// API ROUTES
app.use('/api/auth', authRoute)
app.use('/api/users', userRoute)
app.use('/api/categories', categoriesRoute)
app.use('/api/books', bookRoute)
app.use('/api/epub', epubRoute)
app.use('/api/history', historyRoute)
app.use('/api/feedback', feedbackRoute)

// ADMIN ROUTES
app.use('/api/admin', adminRoutes)


// ERROR HANDLER
app.use((error, req, res, next) => {
  logger.error('Unhandled error:', error)

  let statusCode = error.status || error.statusCode || httpStatus.status.INTERNAL_SERVER_ERROR
  if (!statusCode || typeof statusCode !== 'number') {
    statusCode = 500
  }

  res.status(statusCode).json({
    success: false,
    message: error.message || 'Internal server error'
  })
})

module.exports = app
