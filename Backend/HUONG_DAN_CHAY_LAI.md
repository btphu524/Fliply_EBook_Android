# 🚀 Hướng dẫn chạy lại dự án trên máy khác

Hướng dẫn từng bước để clone và chạy lại dự án Reading Book API trên máy mới.

## 📋 Mục lục

- [Yêu cầu](#-yêu-cầu)
- [Bước 1: Clone repository](#-bước-1-clone-repository)
- [Bước 2: Cài đặt dependencies](#-bước-2-cài-đặt-dependencies)
- [Bước 3: Cấu hình môi trường](#-bước-3-cấu-hình-môi-trường)
- [Bước 4: Sửa các file cần thiết](#-bước-4-sửa-các-file-cần-thiết)
- [Bước 5: Chạy local (Development)](#-bước-5-chạy-local-development)
- [Bước 6: Setup Fly.io](#-bước-6-setup-flyio)
- [Bước 7: Deploy lên Fly.io](#-bước-7-deploy-lên-flyio)
- [Bước 8: Kiểm tra](#-bước-8-kiểm-tra)

---

## ✅ Yêu cầu

Trước khi bắt đầu, đảm bảo bạn đã cài đặt:

- ✅ **Node.js** >= 18.x
- ✅ **npm** >= 9.x
- ✅ **Git**
- ✅ **Fly.io CLI** (cho deployment)

Cài đặt Fly.io CLI:

```bash
# Windows (PowerShell)
powershell -Command "iwr https://fly.io/install.ps1 -useb | iex"

# Mac/Linux
curl -L https://fly.io/install.sh | sh
```

---

## 📥 Bước 1: Clone repository

```bash
# Clone repository về máy
git clone https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git

# Di chuyển vào thư mục dự án
cd YOUR_REPO_NAME

# Hoặc nếu đã có thư mục, clone vào thư mục đó
```

---

## 📦 Bước 2: Cài đặt dependencies

```bash
# Cài đặt tất cả packages
npm install

# Đợi quá trình cài đặt hoàn tất
```

---

## ⚙️ Bước 3: Cấu hình môi trường

### 3.1. Tạo file .env

```bash
# Copy file mẫu
cp env.example .env

# Mở file .env để chỉnh sửa
# Windows
notepad .env
# hoặc
code .env

# Mac/Linux
nano .env
# hoặc
code .env
```

### 3.2. Điền thông tin vào .env

Cập nhật các biến môi trường sau (xem `env.example` để biết tất cả):

```env
# App Configuration
NODE_ENV=development
APP_NAME=Reading Book API
APP_HOST=localhost
APP_PORT=3000

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

⚠️ **Lưu ý**: Thay tất cả giá trị placeholder bằng giá trị thực tế của bạn.

---

## ✏️ Bước 4: Sửa các file cần thiết

### 4.1. Sửa README.md

Mở `README.md`, tìm phần Support (dòng ~460):

```markdown
## 📞 Support
- **GitHub Issues**: [Create Issue](https://github.com/YOUR_USERNAME/YOUR_REPO_NAME/issues)
```

Thay `YOUR_USERNAME` và `YOUR_REPO_NAME` bằng thông tin của bạn.

### 4.2. Sửa fly.toml (Nếu cần)

Mở `fly.toml`, xem dòng 6:

```toml
app = 'readingbook-api-cold-hill-3738'
```

**Chọn một trong hai:**

**Option 1**: Tạo app mới (sẽ tự động cấu hình `fly.toml`)

```bash
flyctl launch --no-deploy
# Sẽ hỏi tên app, chọn tên bạn muốn
```

**Option 2**: Giữ nguyên và tạo app với tên đó

```bash
flyctl apps create readingbook-api-cold-hill-3738
```

### 4.3. (Tùy chọn) Sửa src/app.js

Mở `src/app.js`, tìm dòng ~96:

```javascript
docs: 'https://github.com/your-repo/docs'
```

Thay bằng link GitHub repo của bạn.

---

## 🏃‍♂️ Bước 5: Chạy local (Development)

### 5.1. Chạy development server

```bash
npm run dev
```

Server sẽ chạy tại: `http://localhost:3000`
API endpoint: `http://localhost:3000/api`

### 5.2. Kiểm tra hoạt động

Mở trình duyệt hoặc dùng curl:

```bash
# Test health check
curl http://localhost:3000/health

# Test API
curl http://localhost:3000/api
```

### 5.3. Kiểm tra logs

Nếu có lỗi, xem logs trong terminal để debug.

---

## ☁️ Bước 6: Setup Fly.io

### 6.1. Đăng nhập Fly.io

```bash
# Đăng nhập (sẽ mở trình duyệt)
flyctl auth login

# Kiểm tra đã đăng nhập
flyctl auth whoami
```

### 6.2. Tạo app mới (Nếu chưa có)

```bash
# Xem danh sách apps hiện có
flyctl apps list

# Nếu chưa có app, tạo mới:
flyctl apps create your-app-name

# Hoặc dùng launch để tự động cấu hình:
flyctl launch --no-deploy
```

### 6.3. Set secrets lên Fly.io

```bash
# Set tất cả secrets từ file .env
npm run fly:set-secrets

# Hoặc set từng biến:
flyctl secrets set FIREBASE_PROJECT_ID=your-project-id
flyctl secrets set JWT_SECRET=your-secret-key
# ... các biến khác
```

**Lưu ý**: Script sẽ hỏi xác nhận trước khi set.

### 6.4. Kiểm tra secrets đã set

```bash
# Xem tất cả secrets
npm run fly:secrets

# Hoặc
flyctl secrets list
```

---

## 🚀 Bước 7: Deploy lên Fly.io

### 7.1. Deploy thủ công

```bash
# Deploy lên Fly.io
npm run deploy

# Hoặc
flyctl deploy
```

### 7.2. (Tùy chọn) Setup CI/CD

Nếu muốn tự động deploy khi push code:

**Bước 1: Tạo API Token**

```bash
flyctl tokens create deploy -x 999999h
# Copy token này (chỉ hiển thị 1 lần!)
```

**Bước 2: Thêm Secret vào GitHub**

1. Vào repository trên GitHub
2. Settings → Secrets and variables → Actions
3. New repository secret
4. Name: `FLY_API_TOKEN`
5. Value: Token vừa tạo
6. Add secret

**Bước 3: Push code**

```bash
git add .
git commit -m "Initial setup"
git push origin main
```

Pipeline sẽ tự động deploy!

---

## ✅ Bước 8: Kiểm tra

### 8.1. Kiểm tra status

```bash
# Xem status app
npm run fly:status

# Hoặc
flyctl status
```

### 8.2. Xem logs

```bash
# Xem logs real-time
npm run fly:logs

# Hoặc
flyctl logs
```

### 8.3. Mở trên trình duyệt

```bash
# Mở app trên trình duyệt
npm run fly:open

# Hoặc
flyctl open
```

### 8.4. Test API

```bash
# Test health check
curl https://your-app-name.fly.dev/health

# Test API endpoint
curl https://your-app-name.fly.dev/api
```

---

## 🔧 Troubleshooting

### Lỗi: "Could not find App"

```bash
# Tạo app mới
flyctl apps create your-app-name

# Hoặc sửa fly.toml dòng 6
# app = 'your-app-name'
```

### Lỗi: "Not authenticated"

```bash
# Đăng nhập lại
flyctl auth login
```

### Lỗi: "Secrets not found"

```bash
# Set lại secrets
npm run fly:set-secrets
```

### Lỗi: Port đang được sử dụng (Local)

```bash
# Đổi port trong .env
APP_PORT=3001
```

---

## 📝 Checklist nhanh

- [ ] Clone repository về máy
- [ ] Cài đặt dependencies: `npm install`
- [ ] Tạo file `.env` từ `env.example`
- [ ] Điền đầy đủ thông tin vào `.env`
- [ ] Sửa `README.md` - Link GitHub Issues
- [ ] Sửa `fly.toml` - App name (hoặc tạo app mới)
- [ ] (Tùy chọn) Sửa `src/app.js` - Link docs
- [ ] Test chạy local: `npm run dev`
- [ ] Đăng nhập Fly.io: `flyctl auth login`
- [ ] Tạo app mới: `flyctl apps create app-name`
- [ ] Set secrets: `npm run fly:set-secrets`
- [ ] Deploy: `npm run deploy`
- [ ] Kiểm tra: `npm run fly:status`

---

## 🎯 Tóm tắt các lệnh quan trọng

```bash
# 1. Clone và cài đặt
git clone <repo-url>
cd <repo-name>
npm install

# 2. Cấu hình
cp env.example .env
# Sửa .env với thông tin của bạn

# 3. Test local
npm run dev

# 4. Setup Fly.io
flyctl auth login
flyctl apps create your-app-name
npm run fly:set-secrets

# 5. Deploy
npm run deploy

# 6. Kiểm tra
npm run fly:status
npm run fly:logs
npm run fly:open
```

---

## 💡 Tips

1. **Backup .env**: Lưu file `.env` ở nơi an toàn (không commit lên Git!)
2. **Test local trước**: Luôn test local trước khi deploy
3. **Kiểm tra logs**: Nếu lỗi, xem logs để debug
4. **Secrets**: Đảm bảo tất cả secrets đã được set trên Fly.io
5. **App name**: Mỗi app cần tên unique trên Fly.io

---

**Sau khi hoàn thành các bước trên, dự án sẽ chạy thành công trên máy mới!** 🎉

