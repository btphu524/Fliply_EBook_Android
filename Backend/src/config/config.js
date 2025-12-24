const dotenv = require('dotenv')
dotenv.config()

// Helper function to safely parse environment variables
const safeParse = (value, defaultValue = null) => {
  try {
    return value ? value : defaultValue
  } catch (error) {
    return defaultValue
  }
}

const safeSplit = (value, defaultValue = []) => {
  try {
    return value ? value.split(',') : defaultValue
  } catch (error) {
    return defaultValue
  }
}

module.exports = {
  env: process.env.NODE_ENV || 'development',
  app: {
    name: safeParse(process.env.APP_NAME, 'Reading Book API'),
    host: safeParse(process.env.APP_HOST, '0.0.0.0'),
    port: safeParse(process.env.APP_PORT, 3000),
    apiVersion: safeParse(process.env.API_VERSION, 'v1'),
    prefix: safeParse(process.env.API_PREFIX, '')
  },
  firebase: {
    projectId: process.env.FIREBASE_PROJECT_ID,
    projectNumber: process.env.FIREBASE_PROJECT_NUMBER,
    databaseURL: process.env.FIREBASE_DATABASE_URL,
    webApiKey: process.env.FIREBASE_WEB_API_KEY,
    privateKeyId: process.env.FIREBASE_PRIVATE_KEY_ID,
    privateKey: process.env.FIREBASE_PRIVATE_KEY,
    clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
    clientId: process.env.FIREBASE_CLIENT_ID
  },
  email: {
    provider: process.env.EMAIL_PROVIDER || 'smtp',
    smtp: {
      host: process.env.SMTP_HOST,
      port: parseInt(process.env.SMTP_PORT) || 587,
      auth: {
        user: process.env.SMTP_USERNAME,
        pass: process.env.SMTP_PASSWORD
      }
    },
    sendgrid: {
      apiKey: process.env.SENDGRID_API_KEY
    },
    from: process.env.EMAIL_FROM,
    support: process.env.SUPPORT_EMAIL || process.env.EMAIL_FROM
  },
  upload: {
    limit: safeParse(process.env.UPLOAD_LIMIT, '10mb'),
    allowedFormats: safeSplit(process.env.ALLOWED_FORMATS, ['jpg', 'jpeg', 'png', 'pdf', 'epub']),
    storagePath: safeParse(process.env.STORAGE_PATH, 'uploads/')
  },
  rateLimit: {
    max: parseInt(safeParse(process.env.RATE_LIMIT, '100'), 10),
    windowMs: parseInt(safeParse(process.env.RATE_LIMIT_WINDOW, '15'), 10) * 60 * 1000
  },
  logging: {
    level: safeParse(process.env.LOG_LEVEL, 'info'),
    format: safeParse(process.env.LOG_FORMAT, 'combined')
  },
  cors: {
    origin: safeParse(process.env.CORS_ORIGIN, '*'),
    methods: safeSplit(process.env.CORS_METHODS, ['GET', 'HEAD', 'PUT', 'PATCH', 'POST', 'DELETE']),
    credentials: safeParse(process.env.CORS_CREDENTIALS, 'true') === 'true'
  },
  otp: {
    length: parseInt(process.env.OTP_LENGTH, 10) || 6,
    expiry: parseInt(process.env.OTP_EXPIRY, 10) || 300,
    provider: process.env.OTP_PROVIDER || 'email'
  },
  twilio: {
    accountSid: process.env.TWILIO_ACCOUNT_SID,
    authToken: process.env.TWILIO_AUTH_TOKEN,
    phoneNumber: process.env.TWILIO_PHONE_NUMBER
  },
  cache: {
    ttl: process.env.CACHE_TTL ? parseInt(process.env.CACHE_TTL, 10) : 300,
    checkperiod: process.env.CACHE_CHECKPERIOD ? parseInt(process.env.CACHE_CHECKPERIOD, 10) : 120
  },
  jwt: {
    secret: process.env.JWT_SECRET || 'your-super-secret-jwt-key-change-this-in-production',
    expiry: process.env.JWT_EXPIRY || '24h'
  }
}
