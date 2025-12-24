# 📚 Reading Book API - Backend System

> **Fliply** - Hệ thống backend API cho ứng dụng đọc sách điện tử với Firebase Authentication, Socket.io, OTP, quản lý sách yêu thích và hỗ trợ EPUB.

## 🎯 Tổng quan

**Reading Book API** là hệ thống backend hoàn chỉnh cho ứng dụng đọc sách điện tử, cung cấp các tính năng xác thực, quản lý người dùng, quản lý sách, đọc EPUB và nhiều tính năng khác.

### ✨ Tính năng chính

- 🔐 **Authentication**: Đăng ký, đăng nhập, OTP, quên mật khẩu
- 👤 **User Management**: Quản lý thông tin người dùng, sách yêu thích
- 📖 **Book Management**: Tìm kiếm, phân loại, quản lý sách
- 📚 **EPUB Support**: Đọc và xử lý sách điện tử EPUB
- 📝 **Reading History**: Lịch sử đọc, bookmark, tiến độ
- 💬 **Feedback System**: Đánh giá và phản hồi
- 🛡️ **Admin Panel**: Quản lý hệ thống cho admin
- 🔄 **Real-time**: Socket.io cho tính năng real-time
- 📧 **Email Service**: Gửi email OTP, thông báo

## 📋 Mục lục

- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cài đặt](#-cài-đặt)
- [Cấu hình](#-cấu-hình)
- [Chạy ứng dụng](#-chạy-ứng-dụng)
- [API Documentation](#-api-documentation)
- [Deployment](#-deployment)
  - [CI/CD Pipeline](#-cicd-pipeline-tự-động)
  - [Set Secrets](#-set-secrets-lên-flyio)
  - [Deploy thủ công](#-deploy-thủ-công)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Security](#-security)
- [Troubleshooting](#-troubleshooting)

## 🔧 Yêu cầu hệ thống

- **Node.js**: >= 18.x
- **npm**: >= 9.x
- **Firebase Project**: Cho Authentication và Firestore
- **Resend Account**: Miễn phí để gửi email
- **Fly.io Account**: Để deploy (có free tier)

## 🚀 Cài đặt

```bash
# Clone repository
git clone <repository-url>
cd be-readingbook

# Cài đặt dependencies
npm install

# Tạo file môi trường
cp env.example .env

# Chỉnh sửa .env với thông tin thực tế
nano .env  # hoặc code .env
```

## ⚙️ Cấu hình

### Biến môi trường cần thiết

Xem file `env.example` để biết tất cả các biến môi trường. Dưới đây là các biến quan trọng:

```env
# App Configuration
NODE_ENV=development
APP_NAME=Reading Book API
APP_HOST=localhost
APP_PORT=3000
API_VERSION=v1
API_PREFIX=/api

# Firebase Configuration (BẮT BUỘC)
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_PROJECT_NUMBER=your-project-number
FIREBASE_DATABASE_URL=https://your-project.firebaseio.com
FIREBASE_WEB_API_KEY=your-web-api-key
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@your-project.iam.gserviceaccount.com

# Email Configuration (BẮT BUỘC)
RESEND_API_KEY=re_xxxxxxxxxxxx
EMAIL_FROM=onboarding@resend.dev

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-here
JWT_EXPIRY=24h

# CORS
CORS_ORIGIN=http://localhost:3000,http://localhost:3001
```

### Cấu hình Firebase

1. Tạo project Firebase mới
2. Bật Authentication (Email/Password)
3. Tạo service account và download JSON key
4. Cập nhật các biến `FIREBASE_*` trong `.env`

### Cấu hình Email (Resend API)

1. Đăng ký miễn phí tại: https://resend.com/signup
2. Lấy API Key tại: https://resend.com/api-keys
3. Copy API key và thêm vào file `.env`
4. **Free tier**: 100 emails/ngày, 3,000 emails/tháng

## 🏃‍♂️ Chạy ứng dụng

### Development

```bash
# Chạy development server
npm run dev

# Server sẽ chạy tại: http://localhost:3000
# API endpoint: http://localhost:3000/api
```

### Production

```bash
# Build và chạy production
npm run build
npm run production
```

### Scripts hữu ích

```bash
npm run lint          # Kiểm tra lint
npm run lint:fix       # Tự động sửa lint
npm run build          # Build Babel
npm run test           # Chạy tests
npm run deploy         # Deploy lên Fly.io
npm run fly:status     # Xem status app
npm run fly:logs       # Xem logs
npm run fly:open       # Mở app trên trình duyệt
```

## 📖 API Documentation

### Base URLs

- **Development**: `http://localhost:3000/api`
- **Production**: `https://your-domain.com/api`

### API Endpoints

#### 🔐 Authentication APIs
- `POST /api/auth/register` - Đăng ký tài khoản
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/verify-otp` - Xác thực OTP
- `POST /api/auth/resend-otp` - Gửi lại OTP
- `POST /api/auth/forgot-password` - Quên mật khẩu
- `POST /api/auth/reset-password` - Đặt lại mật khẩu
- `POST /api/auth/change-password` - Đổi mật khẩu
- `POST /api/auth/logout` - Đăng xuất

#### 👤 User APIs
- `GET /api/users` - Lấy thông tin user theo email
- `GET /api/users/:userId` - Lấy thông tin user theo ID
- `PUT /api/users/:userId` - Cập nhật thông tin user
- `GET /api/users/:userId/favorites` - Lấy sách yêu thích
- `POST /api/users/:userId/favorites/:bookId` - Thêm sách yêu thích
- `DELETE /api/users/:userId/favorites/:bookId` - Xóa sách yêu thích

#### 📚 Book APIs
- `GET /api/books` - Lấy danh sách sách (có filter, phân trang)
- `GET /api/books/latest` - Lấy sách mới nhất
- `GET /api/books/:id` - Lấy sách theo ID
- `GET /api/books/search` - Tìm kiếm sách

#### 📂 Category APIs
- `GET /api/categories` - Lấy tất cả danh mục
- `GET /api/categories/:categoryId` - Lấy danh mục theo ID

#### 📖 EPUB APIs
- `POST /api/epub/metadata` - Lấy metadata sách EPUB
- `POST /api/epub/chapters` - Lấy danh sách chương
- `POST /api/epub/chapter-content` - Lấy nội dung chương
- `POST /api/epub/validate-url` - Kiểm tra URL EPUB hợp lệ

#### 📝 History APIs
- `POST /api/history/bookmark` - Lưu bookmark
- `GET /api/history/:userId` - Lấy lịch sử đọc theo user
- `DELETE /api/history/:userId/bookmark/:bookId` - Xóa bookmark

#### 💬 Feedback APIs
- `POST /api/feedback` - Tạo feedback
- `GET /api/feedback/my-feedbacks` - Lấy feedback của user
- `PUT /api/feedback/:id` - Cập nhật feedback
- `DELETE /api/feedback/:id` - Xóa feedback

#### 🛡️ Admin APIs
- `POST /api/admin/categories` - Tạo danh mục mới
- `PUT /api/admin/categories/:categoryId` - Cập nhật danh mục
- `DELETE /api/admin/categories/:categoryId` - Xóa danh mục
- `POST /api/admin/books` - Tạo sách mới
- `PUT /api/admin/books/:id` - Cập nhật sách
- `DELETE /api/admin/books/:id` - Xóa sách
- `GET /api/admin/users` - Quản lý người dùng

### Quick Start Example

```bash
# Đăng ký tài khoản mới
curl -X POST http://localhost:3000/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "fullName": "Nguyễn Văn A",
    "username": "nguyenvana",
    "phoneNumber": "0123456789"
  }'

# Đăng nhập
curl -X POST http://localhost:3000/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'

# Lấy danh sách sách
curl -X GET "http://localhost:3000/api/books?page=1&limit=10" \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN'
```

**Xem tài liệu API chi tiết:** [docs_api/](docs_api/)

## 📦 Deployment

### ⚡ CI/CD Pipeline (Tự động)

Dự án đã được setup **CI/CD tự động** với GitHub Actions. Chỉ cần push code là tự động deploy!

#### Thiết lập ban đầu (chỉ làm 1 lần)

1. **Tạo Fly.io API Token**:
   ```bash
   flyctl auth login
   flyctl tokens create deploy -x 999999h
   # Lưu lại token này (chỉ hiển thị 1 lần!)
   ```

2. **Thêm Secret vào GitHub**:
   - Vào repository → **Settings** → **Secrets and variables** → **Actions**
   - Click **New repository secret**
   - **Name**: `FLY_API_TOKEN`
   - **Value**: Dán token vừa tạo
   - Click **Add secret**

#### Sử dụng hàng ngày

Sau khi setup xong, chỉ cần push code:

```bash
git add .
git commit -m "feat: thêm tính năng mới"
git push origin main
```

Pipeline sẽ tự động:
1. ✅ Deploy lên Fly.io
2. ✅ Xem progress tại tab **Actions** trên GitHub

#### Tính năng CI/CD

- ✅ **Tự động deploy**: Deploy lên Fly.io khi push vào `main`
- ✅ **Docker caching**: Build nhanh hơn 2-3 lần
- ✅ **Bảo mật**: Chạy container với user không phải root
- ✅ **Health check**: Tự động kiểm tra sức khỏe ứng dụng

### 🔑 Set Secrets lên Fly.io

Để set tất cả biến môi trường từ file `.env` lên Fly.io:

```bash
# Cách 1: Dùng npm script (Khuyên dùng)
npm run fly:set-secrets

# Cách 2: Dùng script trực tiếp
node set-secrets.js          # Node.js (Mọi hệ điều hành)
.\set-secrets.ps1            # Windows PowerShell
bash set-secrets.sh          # Mac/Linux
```

#### Cách hoạt động

1. Script đọc file `.env` trong thư mục hiện tại
2. Tự động bỏ qua:
   - Dòng comment (bắt đầu bằng `#`)
   - Dòng trống
   - Giá trị placeholder (như `your-project-id`)
3. Set từng biến lên Fly.io
4. Hiển thị thống kê số biến đã set thành công

#### Lưu ý quan trọng

- ✅ File `.env` phải tồn tại và có giá trị thực tế (không phải placeholder)
- ✅ Script sẽ hỏi xác nhận trước khi set
- ✅ Kiểm tra secrets sau khi set: `npm run fly:secrets`

### 🚀 Deploy thủ công

Nếu muốn deploy thủ công mà không dùng CI/CD:

```bash
# Deploy lên Fly.io
npm run deploy

# Hoặc với các options
npm run deploy:local      # Build local rồi push
npm run deploy:open       # Deploy và mở trình duyệt
```

### Kiểm tra deployment

```bash
# Xem status app
npm run fly:status

# Xem logs real-time
npm run fly:logs

# Mở trên trình duyệt
npm run fly:open

# Xem tất cả secrets
npm run fly:secrets
```

## 📁 Cấu trúc dự án

```
be-readingbook/
├── 📁 src/                          # Source code chính
│   ├── 📄 app.js                   # Express app configuration
│   ├── 📄 index.js                 # Entry point
│   ├── 📁 config/                  # Configuration files
│   ├── 📁 controllers/             # Request handlers
│   ├── 📁 middlewares/             # Custom middlewares
│   ├── 📁 models/                  # Data models
│   ├── 📁 routes/                  # API routes
│   ├── 📁 services/                # Business logic
│   ├── 📁 validations/             # Request validation schemas
│   ├── 📁 providers/               # External service providers
│   ├── 📁 sockets/                 # Socket.io handlers
│   └── 📁 utils/                    # Utility functions
├── 📁 admin/                        # Admin panel APIs
│   ├── 📁 controllers/             # Admin controllers
│   ├── 📁 routes/                  # Admin routes
│   ├── 📁 services/                # Admin services
│   └── 📁 validations/             # Admin validations
├── 📁 docs_api/                     # API documentation
├── 📁 .github/                      # GitHub Actions workflows
│   └── 📁 workflows/
│       └── 📄 fly-deploy.yml       # CI/CD pipeline
├── 📄 Dockerfile                    # Docker configuration
├── 📄 fly.toml                      # Fly.io configuration
├── 📄 package.json                  # Dependencies và scripts
├── 📄 env.example                   # Environment variables example
└── 📄 README.md                     # This file
```

## 🛡️ Security

### Authentication & Authorization
- **Firebase Authentication**: Email/password, OTP verification
- **JWT Tokens**: Secure token-based authentication
- **Role-based Access**: User và Admin roles
- **Password Hashing**: bcrypt với salt rounds

### Security Middleware
- **Helmet**: Security headers
- **CORS**: Cross-origin resource sharing
- **Rate Limiting**: API rate limiting
- **Input Validation**: Joi schema validation

### Data Protection
- **Environment Variables**: Sensitive data in .env
- **Firebase Security Rules**: Database access control
- **HTTPS**: SSL/TLS encryption

## 🔧 Troubleshooting

### Common Issues

#### Port đang được sử dụng
```bash
# Kiểm tra port đang sử dụng
netstat -tulpn | grep :3000

# Thay đổi port trong .env
APP_PORT=3001
```

#### Firebase Authentication lỗi
```bash
# Kiểm tra Firebase configuration
echo $FIREBASE_PROJECT_ID
echo $FIREBASE_PRIVATE_KEY
```

#### Email không gửi được
```bash
# Kiểm tra Resend API key
echo $RESEND_API_KEY

# Logs nên hiển thị: "📧 Using Resend API for email delivery"
```

#### Deploy fail
```bash
# Xem logs chi tiết
npm run fly:logs

# Kiểm tra secrets đã set chưa
npm run fly:secrets
```

### Logs và Debugging

```bash
# Xem logs development
npm run dev

# Xem logs production
npm run production

# Debug mode
DEBUG=* npm run dev
```

## 🤝 Contributing

1. Fork repository
2. Tạo feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push branch: `git push origin feature/amazing-feature`
5. Tạo Pull Request

## 📄 License

Dự án này được cấp phép theo [MIT License](LICENSE).

## 👥 Team

| Tên           | Số điện thoại | Vai Trò   |
| ------------- | ------------- | --------- |
| Vũ Tuấn Kiệt  | 0936992346    | Backend   |

## 📞 Support

- **Email**: taagnes3110@gmail.com
- **GitHub Issues**: [Create Issue](https://github.com/TAAgnes3110/be-readingbook/issues)
- **Documentation**: [API Docs](docs_api/)

---

**Made with ❤️ by Reading Book Team**
