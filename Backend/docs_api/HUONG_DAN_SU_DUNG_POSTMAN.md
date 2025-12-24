## Hướng dẫn sử dụng Postman cho Reading Book API

Tài liệu này giúp bạn thiết lập môi trường, biến, token và ví dụ request/response để team có thể test nhanh các API (bao gồm Admin và User).

### 1) Chuẩn bị môi trường
- **Base URL**: `http://0.0.0.0:8000`
- Tạo một Environment trong Postman (ví dụ: `ReadingBook Local`).
- Thêm các biến Environment sau:
  - `baseUrl`: `http://0.0.0.0:8000`
  - `adminToken`: để trống (sẽ set sau khi login admin)
  - `userToken`: để trống (sẽ set sau khi login user)
  - `userId`: để trống (sau khi đăng ký/tra cứu)
  - `bookId`: để trống (sau khi tạo/tra cứu)
  - `categoryId`: để trống (sau khi tạo/tra cứu)

Khi gọi API, luôn dùng URL dạng: `{{baseUrl}}/api/...`

### 2) Đăng nhập và lưu token tự động

Nếu đã có route login (ví dụ `POST /api/auth/login`), body mẫu:
```json
{
  "email": "admin@example.com",
  "password": "your_password"
}
```
Trong tab Tests của request login admin, thêm script để lưu token vào Environment:
```javascript
const json = pm.response.json();
if (json && json.data && json.data.accessToken) {
  pm.environment.set('adminToken', json.data.accessToken);
}
```
Với login user, lưu vào `userToken`:
```javascript
const json = pm.response.json();
if (json && json.data && json.data.accessToken) {
  pm.environment.set('userToken', json.data.accessToken);
}
```

Sau khi có token, các request Admin/User dùng header:
```
Authorization: Bearer {{adminToken}}
```
hoặc
```
Authorization: Bearer {{userToken}}
```

Bạn cũng có thể cấu hình Authorization tại tab Authorization của collection/folder:
- Type: Bearer Token
- Token: `{{adminToken}}` hoặc `{{userToken}}`

### 3) Quy ước dữ liệu và biến dùng chung

- `bookModel` sử dụng xóa mềm bằng trường `isActive: false`. Khôi phục chỉ thành công khi sách có `isActive === false`.
- Một số trường quan trọng:
  - Sách: `title`, `author`, `category` (số), `description`, `release_date` (ISO hoặc yyyy-MM-dd), `cover_url`, `txt_url`, `book_url`, `epub_url`, `keywords` (mảng string), `status` (`active|inactive|draft`).
  - Thể loại: `name`, `image_url`, `status` (`active|inactive`).
- Với Admin endpoints, luôn cần Bearer `{{adminToken}}`.

### 4) Admin - Book Management

1) Tạo sách mới – `POST {{baseUrl}}/api/admin/books`
Headers:
- `Content-Type: application/json`
- `Authorization: Bearer {{adminToken}}`

Body (raw JSON):
```json
{
  "title": "Tiểu thuyết mới",
  "author": "Tác giả A",
  "description": "Mô tả sách",
  "category": 1,
  "epub_url": "https://example.com/book.epub",
  "cover_url": "https://example.com/cover.jpg",
  "txt_url": "https://example.com/book.txt",
  "book_url": "https://example.com/book.url",
  "keywords": ["1", "2"],
  "status": "active"
}
```
Tests (gợi ý):
```javascript
pm.test('Status 201', () => pm.response.to.have.status(201));
const json = pm.response.json();
pm.environment.set('bookId', json?.data?.book?._id);
pm.test('Success true', () => pm.expect(json.success).to.eql(true));
```

2) Cập nhật sách – `PUT {{baseUrl}}/api/admin/books/:id`
Body chỉ cần các trường muốn cập nhật. Ví dụ:
```json
{ "description": "Mô tả cập nhật" }
```

3) Xóa mềm sách – `DELETE {{baseUrl}}/api/admin/books/:id`
- Thực hiện xóa mềm bằng `bookModel.delete(id)` → `isActive=false`.

4) Khôi phục sách – `POST {{baseUrl}}/api/admin/books/:id/restore`
- Chỉ khôi phục được khi `isActive=false`.

5) Xóa vĩnh viễn – `DELETE {{baseUrl}}/api/admin/books/:id/hard`
- Xóa hẳn bản ghi (không khôi phục).

6) Lấy danh sách sách đã xóa mềm – `GET {{baseUrl}}/api/admin/books/deleted`
- Trả về danh sách sách có `isActive!==true` hoặc có `deletedAt`.

### 5) Admin - Category Management

1) Tạo category – `POST {{baseUrl}}/api/admin/categories`
Body:
```json
{
  "name": "Văn học",
  "image_url": "https://example.com/cat.png",
  "status": "active"
}
```
Tests (gợi ý):
```javascript
pm.test('Status 201', () => pm.response.to.have.status(201));
const json = pm.response.json();
pm.environment.set('categoryId', json?.data?.categoryId);
```

2) Cập nhật category – `PUT {{baseUrl}}/api/admin/categories/:categoryId`
Body ví dụ:
```json
{ "name": "Văn học Việt Nam" }
```

3) Xóa mềm category – `DELETE {{baseUrl}}/api/admin/categories/:categoryId`
- Service admin set `status='inactive'` và `deletedAt`.

4) Khôi phục category – `POST {{baseUrl}}/api/admin/categories/:categoryId/restore`
- Set `status='active'`, `deletedAt=null`.

5) Xóa vĩnh viễn – `DELETE {{baseUrl}}/api/admin/categories/:categoryId/hard`

6) Danh sách đã xóa mềm – `GET {{baseUrl}}/api/admin/categories/deleted`

### 6) User - Một số endpoint thường dùng

1) Lưu bookmark (lịch sử đọc) – `POST {{baseUrl}}/api/history/bookmark`
Headers: `Authorization: Bearer {{userToken}}`
Body:
```json
{
  "userId": {{userId}},
  "bookId": {{bookId}},
  "chapterId": "chapter-001"
}
```

2) Lấy lịch sử đọc – `GET {{baseUrl}}/api/history/:userId?page=1&limit=10&sortBy=lastReadAt&sortOrder=desc`
Headers: `Authorization: Bearer {{userToken}}`

3) Lấy bookmark theo sách – `GET {{baseUrl}}/api/history/:userId/bookmark/:bookId`

### 7) Thiết lập Header mặc định cho Collection

Trong tab Authorization của Collection:
- Admin folder: Type = Bearer Token, Token = `{{adminToken}}`.
- User folder: Type = Bearer Token, Token = `{{userToken}}`.

Trong tab Variables của Collection/Folder có thể khai báo mặc định: `bookId`, `categoryId`, `userId` để reuse trong body/path.

### 8) Lỗi thường gặp và cách khắc phục

- 400 JSON parse: “Expected ',' or '}' …” → Body không phải JSON hợp lệ. Chọn Body → raw → JSON và sửa dấu phẩy/ngoặc.
- 401 Unauthorized: token trống/hết hạn → Login lại và đảm bảo header `Authorization: Bearer {{...Token}}` đang đúng Environment.
- 403 Forbidden: với lịch sử đọc, chỉ xem được của chính mình (`:userId` phải trùng token user).
- 400 khi restore book: sách chưa bị xóa mềm theo chuẩn `isActive=false` → hãy DELETE bằng endpoint xóa mềm trước rồi mới RESTORE.

### 9) Mẹo viết Tests trong Postman

Mẫu test chung:
```javascript
pm.test('HTTP 2xx', () => pm.response.code >= 200 && pm.response.code < 300);
const json = pm.response.json();
pm.test('Có trường success', () => pm.expect(json).to.have.property('success'));
```

Lưu biến từ response:
```javascript
const json = pm.response.json();
if (json?.data?.book?._id) pm.environment.set('bookId', json.data.book._id);
if (json?.data?.categoryId) pm.environment.set('categoryId', json.data.categoryId);
```

So khớp request vs response:
```javascript
const req = pm.request.body?.raw ? JSON.parse(pm.request.body.raw) : {};
const res = pm.response.json();
pm.test('Title khớp', () => pm.expect(res?.data?.book?.title).to.eql(req.title));
```

---
Tài liệu này nhằm chuẩn hóa cách team sử dụng Postman, đảm bảo cùng một bộ biến/headers và luồng CRUD chuẩn cho Admin và User. Nếu phát sinh endpoint mới, hãy bổ sung vào tài liệu theo cùng cấu trúc trên.


