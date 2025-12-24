const validate = (schema) => {
  return (req, res, next) => {
    const errors = []

    // Validate body
    if (schema.body) {
      const { error } = schema.body.validate(req.body)
      if (error) {
        errors.push(...error.details.map(detail => ({
          message: detail.message,
          path: detail.path,
          type: detail.type,
          source: 'body'
        })))
      }
    }

    // Validate params
    if (schema.params) {
      const { error } = schema.params.validate(req.params)
      if (error) {
        errors.push(...error.details.map(detail => ({
          message: detail.message,
          path: detail.path,
          type: detail.type,
          source: 'params'
        })))
      }
    }

    // Validate query
    if (schema.query) {
      const { error } = schema.query.validate(req.query)
      if (error) {
        errors.push(...error.details.map(detail => ({
          message: detail.message,
          path: detail.path,
          type: detail.type,
          source: 'query'
        })))
      }
    }

    if (errors.length > 0) {
      return res.status(400).json({
        success: false,
        message: errors[0].message, // Trả về message đầu tiên để dễ hiểu
        errors: errors
      })
    }
    next()
  }
}

module.exports = validate
