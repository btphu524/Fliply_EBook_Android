/**
 * select allowed keys from an object
 * @param {Object} object
 * @param {string[]} keys
 * @returns {Object}
 * example:
 * const obj = { email: 'taagnes@gmail.com', password: '123', role : 'admin'};
 * keys = [email, password];
 * const picked = pick(obj, keys); => result { email: 'taagnes@gmail.com', password: '123' }
 */
const pick = (object, keys) => {
  return keys.reduce((obj, key) => {
    if (object && Object.prototype.hasOwnProperty.call(object, key)) {
      obj[key] = object[key]
    }
    return obj
  }, {})
}

module.exports = pick
