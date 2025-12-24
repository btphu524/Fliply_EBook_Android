const authValidation = require('./authValidation')
const userValidation = require('./userValidation')
const bookValidation = require('./bookValidation')
const categoriesValidation = require('./categoriesValidation')
const epubValidation = require('./epubValidation')
const historyValidation = require('./historyValidation')
const feedbackValidation = require('./feedbackValidation')
const custom = require('./custom')

module.exports = {
  authValidation,
  userValidation,
  bookValidation,
  categoriesValidation,
  epubValidation,
  historyValidation,
  feedbackValidation,
  custom
}
