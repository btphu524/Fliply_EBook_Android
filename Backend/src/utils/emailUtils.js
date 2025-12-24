/**
 * Utility functions for email encoding/decoding for Firebase paths
 */

/**
 * Encode email address to be Firebase Realtime Database path-safe
 * Replaces invalid characters with safe alternatives
 * @param {string} email - Email address to encode
 * @returns {string} - Encoded email safe for Firebase paths
 */
function encodeEmailForFirebase(email) {
  if (!email) return ''

  return email
    .toLowerCase()
    .replace(/\./g, '_DOT_')      // Replace dots with _DOT_
    .replace(/@/g, '_AT_')        // Replace @ with _AT_
    .replace(/\+/g, '_PLUS_')     // Replace + with _PLUS_
    .replace(/-/g, '_DASH_')      // Replace - with _DASH_
    .replace(/:/g, '_COLON_')     // Replace : with _COLON_
    .replace(/#/g, '_HASH_')      // Replace # with _HASH_
    .replace(/\$/g, '_DOLLAR_')   // Replace $ with _DOLLAR_
    .replace(/\[/g, '_LBRACKET_') // Replace [ with _LBRACKET_
    .replace(/\]/g, '_RBRACKET_') // Replace ] with _RBRACKET_
}

/**
 * Decode email address from Firebase path format back to original
 * @param {string} encodedEmail - Encoded email from Firebase path
 * @returns {string} - Original email address
 */
function decodeEmailFromFirebase(encodedEmail) {
  if (!encodedEmail) return ''

  return encodedEmail
    .replace(/_DOT_/g, '.')       // Replace _DOT_ with dots
    .replace(/_AT_/g, '@')        // Replace _AT_ with @
    .replace(/_PLUS_/g, '+')      // Replace _PLUS_ with +
    .replace(/_DASH_/g, '-')      // Replace _DASH_ with -
    .replace(/_COLON_/g, ':')     // Replace _COLON_ with :
    .replace(/_HASH_/g, '#')      // Replace _HASH_ with #
    .replace(/_DOLLAR_/g, '$')    // Replace _DOLLAR_ with $
    .replace(/_LBRACKET_/g, '[')  // Replace _LBRACKET_ with [
    .replace(/_RBRACKET_/g, ']')  // Replace _RBRACKET_ with ]
}

/**
 * Create a Firebase-safe key for email-based operations
 * @param {string} email - Email address
 * @param {string} prefix - Optional prefix (default: 'email')
 * @returns {string} - Firebase-safe key
 */
function createEmailKey(email, prefix = 'email') {
  const encodedEmail = encodeEmailForFirebase(email)
  return `${prefix}:${encodedEmail}`
}

/**
 * Extract email from Firebase key
 * @param {string} key - Firebase key with encoded email
 * @param {string} prefix - Optional prefix (default: 'email')
 * @returns {string} - Original email address
 */
function extractEmailFromKey(key, prefix = 'email') {
  if (!key || !key.startsWith(`${prefix}:`)) return ''

  const encodedEmail = key.substring(prefix.length + 1) // Remove prefix and colon
  return decodeEmailFromFirebase(encodedEmail)
}

module.exports = {
  encodeEmailForFirebase,
  decodeEmailFromFirebase,
  createEmailKey,
  extractEmailFromKey
}
