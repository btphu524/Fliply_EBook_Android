const nodemailer = require('nodemailer')
const config = require('../config/config')
const logger = require('../config/logger')

let transporter = null

/**
 * Initialize SMTP email transporter
 */
function initializeSMTP() {
  if (!config.email.smtp.host || !config.email.smtp.auth.user) {
    logger.warn('SMTP configuration missing')
    return false
  }

  try {
    const port = parseInt(config.email.smtp.port) || 587
    transporter = nodemailer.createTransport({
      host: config.email.smtp.host,
      port: port,
      secure: port === 465,
      auth: {
        user: config.email.smtp.auth.user,
        pass: config.email.smtp.auth.pass
      },

      connectionTimeout: 10000,
      greetingTimeout: 10000,
      socketTimeout: 10000,
      tls: {
        rejectUnauthorized: config.env === 'production'
      }
    })
    logger.info(`✅ SMTP email transporter initialized (${config.email.smtp.host}:${port})`)
    return true
  } catch (error) {
    logger.error(`❌ Failed to initialize SMTP: ${error.message}`)
    return false
  }
}

/**
 * Initialize email provider
 */
function initialize() {
  const smtpInitialized = initializeSMTP()
  if (!smtpInitialized) {
    logger.warn('⚠️  No email provider configured! Emails will not be sent.')
  }
}

/**
 * Send email using SMTP
 * @param {string} email
 * @param {string} subject
 * @param {string} html
 * @returns {Promise<{success: boolean, messageId: string}>}
 */
async function send(email, subject, html) {
  if (!transporter) {
    logger.error('❌ Email transporter not initialized')
    throw new Error('Email service not configured. Please set SMTP credentials.')
  }

  const mailOptions = {
    from: config.email.from,
    to: email,
    subject,
    html
  }

  try {
    const result = await transporter.sendMail(mailOptions)
    logger.info(`✅ Email sent via SMTP to ${email}: ${result.messageId}`)
    return { success: true, messageId: result.messageId }
  } catch (error) {
    logger.error(`❌ Failed to send email via SMTP to ${email}: ${error.stack}`)
    throw error
  }
}

/**
 * Kiểm tra kết nối email service
 * @returns {Promise<boolean>}
 */
async function verifyConnection() {
  if (!transporter) {
    logger.error('❌ Email transporter not available for verification')
    return false
  }

  try {
    await transporter.verify()
    logger.info('✅ SMTP connection verified successfully')
    return true
  } catch (error) {
    logger.error(`❌ SMTP connection verification failed: ${error.message}`)
    return false
  }
}

initialize()

module.exports = {
  send,
  verifyConnection
}
