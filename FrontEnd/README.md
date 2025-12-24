# MyReadBookApplication

🚀 **Giới thiệu**

MyReadBookApplication là một ứng dụng Android được phát triển cho phép người dùng đăng ký, đăng nhập, xác thực OTP và quản lý thông tin tài khoản đọc sách. Đây là phần **Frontend** được xây dựng bằng Java, kết nối với **Backend API** thông qua Retrofit để xử lý các chức năng chính.

---

📂 **Cấu trúc thư mục chính**

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/myreadbookapplication/
│   │   │   ├── model/          # Các model (ApiResponse, SignUpRequest, ...)
│   │   │   ├── network/        # RetrofitClient
│   │   │   ├── ui/             # Activity, Fragment
│   │   │   └── utils/          # Tiện ích (nếu có)
│   │   └── res/                # Layout XML, drawable, values
│   └── AndroidManifest.xml
├── build.gradle
└── README.md
```

---

⚙️ **Công nghệ sử dụng**

- **Ngôn ngữ**: Java
- **UI**: XML Layout + ViewBinding
- **API call**: Retrofit + OkHttp + Gson
- **Authentication**: Xác thực OTP
- **State management**: SharedPreferences

---

🔧 **Cài đặt & Chạy dự án**

### 1. Clone repository
```bash
git clone https://github.com/<your-username>/<repo-name>.git
```

### 2. Mở dự án bằng Android Studio
- Mở **File > Open** và chọn thư mục dự án vừa clone.

### 3. Sync Gradle
- Chờ Android Studio tải các dependencies được khai báo trong `build.gradle`.

### 4. Cấu hình API URL
- Trong file `build.gradle.kts` (module `:app`), URL API đã được khai báo mặc định:
  ```kotlin
  buildConfigField("String", "BASE_URL", "\"http://192.168.1.204:9000/\"")
  ```
- **Lưu ý**:
  - Nếu Backend chạy trên máy khác, cloud, hoặc localhost trên emulator/Genymotion, hãy chỉnh sửa `BASE_URL` tương ứng:
    - Sử dụng `http://10.0.2.2:9000/` khi chạy trên emulator (thay vì địa chỉ IP máy chủ).
    - Cập nhật IP hoặc domain nếu Backend chạy trên máy chủ khác.

### 5. Chạy ứng dụng
- Chọn **Run > Run 'app'** để chạy trên emulator hoặc thiết bị thật.
- Đảm bảo emulator (VD: AVD trong Android Studio) hoặc Genymotion đã được cấu hình đúng.

---

📱 **Các màn hình chính**

- **Sign Up**: Màn hình đăng ký tài khoản với các trường thông tin cơ bản (tên, email, số điện thoại, mật khẩu).
- **Sign In**: Màn hình đăng nhập với email và mật khẩu.
- **Verification (OTP)**: Màn hình nhập mã OTP, bao gồm:
  - Đồng hồ đếm ngược 3 phút.
  - Tùy chọn "Resend OTP" để gửi lại mã nếu cần.

---

🔗 **API Backend liên kết**

- **Repo Backend**: [link đến repo backend] (vui lòng cập nhật link thực tế).
- **Base URL**: `http://<ip>:9000/api/v1/...` (tùy thuộc vào cấu hình server).

---

📌 **Ghi chú**

- Ứng dụng tương thích với **Android 7.0+ (API 24)**.
- Khi test trên emulator hoặc Genymotion:
  - Thay `BASE_URL` thành `http://10.0.2.2:9000/` nếu Backend chạy trên localhost của máy chủ.
- Đảm bảo server Backend đang hoạt động và API (đăng ký, đăng nhập, xác thực OTP) đã được triển khai.
- Cần cấu hình đúng IP hoặc domain trong `BASE_URL` nếu chạy trên mạng khác nhau.

---