const password = (value, helpers) => {
  if (value.length < 8) {
    return helpers.message('Mật khẩu phải có ít nhất 8 ký tự')
  }
  if (!value.match(/\d/) || !value.match(/[a-zA-Z]/)) {
    return helpers.message(
      'Mật khẩu phải chứa ít nhất 1 chữ cái và 1 số'
    )
  }
  return value
}

const confirmPassword = (value, helpers) => {
  const { newPassword, password } = helpers.state.ancestors[0]
  const passwordToCompare = newPassword || password
  if (value !== passwordToCompare) {
    return helpers.message('Xác nhận mật khẩu phải khớp với mật khẩu')
  }
  return value
}

module.exports = {
  password,
  confirmPassword
}
