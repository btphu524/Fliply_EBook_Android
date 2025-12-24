# Project1
# Fliply - Ứng Dụng Đọc Sách Điện Tử Trên Android

Fliply là ứng dụng đọc sách điện tử trên Android, tập trung vào sách tiếng Anh, hỗ trợ học ngoại ngữ và cá nhân hóa trải nghiệm đọc.

## Mục Lục
- [Tổng Quan](#tổng-quan)
- [Tính Năng](#tính-năng)
- [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
- [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
- [Cài Đặt](#cài-đặt)
- [Hướng Dẫn Sử Dụng](#hướng-dẫn-sử-dụng)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Đóng Góp](#đóng-góp)
- [Thành Viên Nhóm](#thành-viên-nhóm)
- [Giấy Phép](#giấy-phép)

## Tổng Quan
Fliply là ứng dụng đọc sách điện tử trên Android, giúp người dùng truy cập kho sách tiếng Anh phong phú, hỗ trợ học ngoại ngữ và cung cấp các tính năng như đánh dấu trang, tìm kiếm thông minh và đồng bộ tiến độ đọc. Dự án được phát triển bởi Nhóm 8 tại Trường Đại học Giao thông Vận tải, dưới sự hướng dẫn của TS. Nguyễn Trọng Phúc, trong tháng 9 năm 2024.

## Tính Năng

### Dành Cho Người Dùng
- Đăng ký/đăng nhập bằng email/mật khẩu, hỗ trợ khôi phục mật khẩu.
- Quản lý thư viện cá nhân: Thêm, xóa, xem sách yêu thích.
- Đọc sách định dạng PDF và TXT, hỗ trợ đánh dấu trang.
- Tìm kiếm sách theo tiêu đề, tác giả, thể loại hoặc từ khóa.
- Cá nhân hóa: Xem lịch sử đọc, gửi đánh giá ứng dụng.

### Dành Cho Quản Trị Viên
- Quản lý sách và thể loại: Thêm, chỉnh sửa, xóa.
- Xem và quản lý đánh giá từ người dùng.
- Tìm kiếm nâng cao theo tiêu đề, tác giả, thể loại.

### Tính Năng Phi Chức Năng
- **Hiệu Năng**: Tải sách trong 3 giây, hỗ trợ 500 người dùng đồng thời.
- **Bảo Mật**: Mã hóa mật khẩu (SHA-256/bcrypt), Firebase Authentication, JWT cho API.
- **Tương Thích**: Hỗ trợ Android 10 trở lên, tương thích với nhiều kích thước màn hình.

## Công Nghệ Sử Dụng
- **Frontend**: Android Studio (Java, XML), Figma (thiết kế UI/UX).
- **Backend**: Node.js (Express.js), Firebase (Realtime Database, Authentication).
- **Server**: Render (triển khai đám mây, CI/CD).
- **Quản Lý Mã Nguồn**: GitHub (Git, GitHub Actions).
- **Kiểm Thử**: Postman (API), Android Studio Emulator, Fiddler, Firebase Performance Monitoring.
- **Quản Lý Dự Án**: ClickUp, Discord, Messenger.
- **Tài Liệu & Sơ Đồ**: Microsoft Word, Draw.io.

## Yêu Cầu Hệ Thống
- **Hệ Điều Hành**: Android 10 trở lên.
- **Môi Trường Phát Triển**:
  - Android Studio (phiên bản mới nhất).
  - Node.js (cho backend).
  - Tài khoản Firebase (cho Authentication và Firestore).
- **Phần Cứng**: Tối thiểu 4GB RAM cho phát triển, thiết bị Android hoặc giả lập.
- **Internet**: Cần kết nối để xác thực, gọi API và triển khai.

## Cài Đặt
- Cài đặt [Android Studio](https://developer.android.com/studio).
- Thiết lập dự án Firebase và kích hoạt Authentication/Firestore.
- Cài đặt [Node.js](https://nodejs.org/).
- Cài đặt [Git](https://git-scm.com/).
- (Tùy chọn) Tài khoản [Render](https://render.com/) để triển khai backend.

## Hướng Dẫn Sử Dụng
1. **Đăng Ký/Đăng Nhập**:
   - Mở ứng dụng, đăng ký bằng email/mật khẩu.
   - Xác minh email bằng OTP từ Firebase.
   - Đăng nhập để vào giao diện chính.

2. **Duyệt và Đọc Sách**:
   - Tìm sách bằng thanh tìm kiếm (tiêu đề, tác giả, thể loại).
   - Thêm sách vào thư viện cá nhân hoặc đọc trực tiếp.
   - Đánh dấu trang để quay lại sau.

3. **Quản Lý Thư Viện**:
   - Xem/xóa/thêm sách yêu thích trong phần Thư Viện.
   - Kiểm tra lịch sử đọc và tiến độ.

4. **Tính Năng Quản Trị** (yêu cầu tài khoản admin):
   - Đăng nhập để quản lý sách, thể loại, đánh giá.
   - Sử dụng bảng điều khiển để thêm/chỉnh sửa/xóa nội dung.

5. **Phản Hồi**:
   - Đánh giá ứng dụng qua tính năng trong ứng dụng.

## Cấu Trúc Dự Án
fliply/<br>
├── app/                    # Mã nguồn ứng dụng Android<br>
│   ├── src/<br>
│   │   ├── main/<br>
│   │   │   ├── java/       # Mã Java cho logic<br>
│   │   │   ├── res/        # XML bố cục và tài nguyên<br>
│   │   │   └── AndroidManifest.xml<br>
│   └── build.gradle        # Cấu hình Gradle<br>
├── backend/                # Mã nguồn backend Node.js<br>
│   ├── src/<br>
│   │   └── config/         # Tệp cấu hình<br>
│   │   ├── routes/         # Tuyến API<br>
│   │   ├── controllers/    # Logic API<br>
│   │   └── models/         # Mô hình dữ liệu<br>
│   │   └── middlewares/     # Middleware tùy chỉnh<br>
│   │   └── providers/       # Nhà cung cấp dữ liệu<br>
│   │   └── services/        # Xử lý logic nghiệp vụ<br>
│   │   └── sockets/         # Trình xử lý Socket.IO<br>
│   │   └── upload/          # Xử lý tải lên tệp<br>
│   │   └── utils/           # Hàm tiện ích<br>
│   │   └── validations/     # Lược đồ kiểm tra dữ liệu đầu vào<br>
│   └── package.json        # Phụ thuộc Node.js<br>
├── docs/                   # Tài liệu<br>
│   ├── postman/            # Bộ sưu tập Postman<br>
│   ├── diagrams/           # Sơ đồ Draw.io<br>
│   └── specification.docx  # Tài liệu đặc tả<br>
├── .gitignore              # Tệp bỏ qua Git<br>
└── README.md               # Tệp này<br>

## Đóng Góp
Chúng tôi hoan nghênh mọi đóng góp để cải thiện Fliply! Để tham gia:
1. Fork kho lưu trữ.
2. Tạo nhánh mới:
   ```bash
   git checkout -b feature/tên-tính-năng
3. Thực hiện thay đổi và commit:
   git commit -m "Mô tả thay đổi"
5. Push lên fork:
   git push origin feature/tên-tính-năng

## Thành Viên Nhóm
| Tên              | Mã Sinh Viên | Vai Trò           |
|------------------|--------------|-------------------|
| Bùi Thanh Phú    | 223630702    | Project Management|
| Bùi Đức Anh      | 223630666    | Tester            |
| Vũ Tuấn Kiệt     | 223630694    | Backend           |
| Vũ Quyết Tiến    | 223630716    | Frontend          |
| Đỗ Hoàng Tùng    | 223630721    | DevOps            |

## Giấy Phép
Dự án này được cấp phép theo [Giấy phép MIT](LICENSE).
Giấy phép MIT

Bản quyền (c) 2025, Bùi Thanh Phú - 223630702, Bùi Đức Anh - 223630666, Vũ Tuấn Kiệt - 223630694, Vũ Quyết Tiến - 223630716, Đỗ Hoàng Tùng - 223630721, Sinh viên trường Đại học Giao Thông Vận Tải - Khoa Công Nghệ Thông Tin - Ngành Khoa Học Máy Tính.

Được phép miễn phí cho bất kỳ ai nhận được bản sao của phần mềm này và các tài liệu liên quan (gọi là "Phần mềm"), để sử dụng Phần mềm mà không bị hạn chế, bao gồm quyền sử dụng, sao chép, chỉnh sửa, hợp nhất, xuất bản, phân phối, cấp phép phụ, hoặc bán bản sao của Phần mềm, với điều kiện:

Thông báo bản quyền ở trên và thông báo cấp phép này phải được bao gồm trong tất cả các bản sao hoặc phần đáng kể của Phần mềm.

PHẦN MỀM ĐƯỢC CUNG CẤP "NGUYÊN VẸN", KHÔNG CÓ BẢO HÀNH NÀO, DÙ RÕ RÀNG HAY NGỤ Ý, BAO GỒM NHƯNG KHÔNG GIỚI HẠN Ở BẢO HÀNH VỀ TÍNH THƯƠNG MẠI, SỰ PHÙ HỢP VỚI MỤC ĐÍCH CỤ THỂ VÀ KHÔNG VI PHẠM. TÁC GIẢ HOẶC CHỦ SỞ HỮU BẢN QUYỀN KHÔNG CHỊU TRÁCH NHIỆM VỀ BẤT KỲ KHIẾU NẠI, THIỆT HẠI HOẶC TRÁCH NHIỆM KHÁC, DÙ TRONG HỢP ĐỒNG, HÀNH VI VI PHẠM HAY CÁC VẤN ĐỀ KHÁC, PHÁT SINH TỪ VIỆC SỬ DỤNG PHẦN MỀM.
