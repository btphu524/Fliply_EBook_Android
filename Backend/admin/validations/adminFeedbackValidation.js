const Joi = require('joi')

const getAllFeedbacksQuery = Joi.object({
  page: Joi.number().integer().min(1).default(1),
  limit: Joi.number().integer().min(1).max(100).default(10),
  sortBy: Joi.string().valid('createdAt', 'updatedAt', 'status').default('createdAt'),
  sortOrder: Joi.string().valid('asc', 'desc').default('desc'),
  status: Joi.string().valid('pending', 'resolved', 'rejected').optional()
})

module.exports = {
  getAllFeedbacksQuery
}
