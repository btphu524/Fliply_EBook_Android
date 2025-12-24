#!/usr/bin/env node
/* eslint-disable no-console */
/**
 * Script Node.js để set tất cả biến môi trường từ file .env lên Fly.io
 * Sử dụng: node set-secrets.js
 */

const fs = require('fs')
const { execSync } = require('child_process')

const ENV_FILE = '.env'

// Kiểm tra file .env
if (!fs.existsSync(ENV_FILE)) {
  console.error('❌ File .env không tồn tại!')
  console.log('💡 Tạo file .env từ env.example: cp env.example .env')
  process.exit(1)
}

console.log(`📖 Đang đọc file ${ENV_FILE}...\n`)

// Kiểm tra flyctl
try {
  execSync('flyctl version', { stdio: 'ignore' })
} catch (error) {
  console.error('❌ flyctl chưa được cài đặt!')
  process.exit(1)
}

// Kiểm tra đã đăng nhập chưa
try {
  execSync('flyctl auth whoami', { stdio: 'ignore' })
} catch (error) {
  console.log('⚠️  Chưa đăng nhập Fly.io')
  execSync('flyctl auth login', { stdio: 'inherit' })
}

// Đọc file .env
const envContent = fs.readFileSync(ENV_FILE, 'utf-8')
const lines = envContent.split('\n')

// Lọc và parse các biến
const variables = []
let total = 0

lines.forEach((line) => {
  const trimmed = line.trim()

  // Bỏ qua comment và dòng trống
  if (trimmed.startsWith('#') || trimmed === '') {
    return
  }

  // Kiểm tra có dấu = không
  if (trimmed.includes('=')) {
    total++
    const equalIndex = trimmed.indexOf('=')
    const key = trimmed.substring(0, equalIndex).trim()
    let value = trimmed.substring(equalIndex + 1).trim()

    // Bỏ quotes nếu có
    if ((value.startsWith('"') && value.endsWith('"')) ||
        (value.startsWith('\'') && value.endsWith('\''))) {
      value = value.slice(1, -1)
    }

    // Bỏ qua nếu key hoặc value rỗng
    if (!key || !value) {
      return
    }

    // Bỏ qua các giá trị placeholder
    if (value.startsWith('your-') ||
        value === 'your-project-id' ||
        value === 'your-super-secret-jwt-key-here') {
      console.log(`⏭️  Bỏ qua ${key} (chưa được cấu hình)`)
      return
    }

    variables.push({ key, value })
  }
})

console.log(`🔍 Tìm thấy ${total} biến môi trường`)
console.log(`📝 Có ${variables.length} biến hợp lệ để set\n`)

// Xác nhận
const readline = require('readline')
const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
})

rl.question('❓ Bạn có muốn set tất cả biến này lên Fly.io? (y/n): ', (answer) => {
  if (answer.toLowerCase() !== 'y') {
    console.log('❌ Đã hủy')
    rl.close()
    process.exit(0)
  }

  console.log('\n🚀 Bắt đầu set secrets...\n')

  let count = 0
  let failed = 0

  // Set từng biến
  variables.forEach(({ key, value }) => {
    process.stdout.write(`⚙️  Đang set ${key}... `)

    try {
      // Escape value nếu cần
      const escapedValue = value.replace(/"/g, '\\"')
      execSync(`flyctl secrets set "${key}=${escapedValue}"`, {
        stdio: 'ignore',
        shell: true
      })
      console.log('✅')
      count++
    } catch (error) {
      console.log('❌')
      failed++
    }
  })

  console.log('\n✅ Hoàn thành!')
  console.log('📊 Thống kê:')
  console.log(`   - Đã set: ${count} biến`)
  console.log(`   - Thất bại: ${failed} biến`)
  console.log('\n🔍 Xem tất cả secrets: flyctl secrets list\n')

  rl.close()
})

