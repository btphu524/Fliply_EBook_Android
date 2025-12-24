const admin = require('firebase-admin')
const config = require('./config')
const logger = require('./logger')

let db = null
let auth = null

try {
  const serviceAccount = {
    type: 'service_account',
    project_id: config.firebase.projectId,
    private_key_id: config.firebase.privateKeyId,
    private_key: config.firebase.privateKey?.replace(/\\n/g, '\n'),
    client_email: config.firebase.clientEmail,
    client_id: config.firebase.clientId,
    auth_uri: 'https://accounts.google.com/o/oauth2/auth',
    token_uri: 'https://oauth2.googleapis.com/token',
    auth_provider_x509_cert_url: 'https://www.googleapis.com/oauth2/v1/certs',
    client_x509_cert_url: `https://www.googleapis.com/robot/v1/metadata/x509/${encodeURIComponent(config.firebase.clientEmail)}`
  }

  if (!admin.apps.length) {
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
      databaseURL: config.firebase.databaseURL
    })
  }

  db = admin.database()
  auth = admin.auth()
  logger.info('✅ Firebase initialized successfully')
} catch (error) {
  logger.error('❌ Firebase initialization failed:', error.message)
}

module.exports = {
  admin,
  db,
  auth,

  getRef: (path) => db ? db.ref(path) : null,

  createUser: (userRecord) => auth ? auth.createUser(userRecord) : Promise.reject(new Error('Firebase not initialized')),
  getUser: (uid) => auth ? auth.getUser(uid) : Promise.reject(new Error('Firebase not initialized')),
  deleteUser: (uid) => auth ? auth.deleteUser(uid) : Promise.reject(new Error('Firebase not initialized')),
  getUserByEmail: (email) => auth ? auth.getUserByEmail(email) : Promise.reject(new Error('Firebase not initialized'))
}
