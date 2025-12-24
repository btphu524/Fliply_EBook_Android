# 📚 Book Data Upload to Firebase

Thư mục này chứa các file và script để upload dữ liệu sách lên Firebase.

## 📁 Files

- `book_firebase.json` - File đã convert cho Firebase (742 books với ID 1-742, categories 1-12)
- `book_test.json` - File test với 100 books đầu tiên
- `categories_fixed.json` - 12 categories với ID 1-12 và link ảnh đẹp
- `convertBookData.js` - Script convert dữ liệu
- `fixBookIds.js` - Script sửa ID từ 1 đến hết
- `fixCategories.js` - Script sửa categories với ID 1-12
- `uploadToFirebase.js` - Script upload lên Firebase

## 🔄 Cấu trúc dữ liệu đã convert

```json
{
  "_id": 2701,
  "title": "Moby Dick; Or, The Whale",
  "author": "Herman Melville", 
  "category": 1,
  "categoryName": "Fiction",
  "description": "Free kindle book and epub digitized and proofread by volunteers.",
  "release_date": "2001-06-30T17:00:00.000Z",
  "cover_url": "https://www.gutenberg.org/cache/epub/2701/pg2701.cover.medium.jpg",
  "txt_url": "https://www.gutenberg.org/files/2701/2701-0.txt",
  "book_url": "https://www.gutenberg.org/ebooks/2701",
  "epub_url": "https://www.gutenberg.org/ebooks/2701.epub.noimages",
  "keywords": ["Whaling", "Sea stories", "Psychological fiction"],
  "status": "active",
  "createdAt": "2025-10-04T07:38:12.058Z",
  "updatedAt": "2025-10-04T07:38:12.058Z",
  "avgRating": 3.2291189059160557,
  "numberOfReviews": 213
}
```

## 🚀 Cách sử dụng

### 1. Convert dữ liệu (đã chạy rồi)
```bash
cd src/upload
node convertBookData.js
```

### 2. Upload test (10 books)
```bash
node uploadToFirebase.js test
```

### 3. Upload đầy đủ (742 books)
```bash
node uploadToFirebase.js full
```

### 4. Xóa tất cả books (cẩn thận!)
```bash
node uploadToFirebase.js clear
```

## 📊 Thống kê dữ liệu

- **Tổng số books**: 742 (ID từ 1-742)
- **Categories**: 12 loại (ID từ 1-12)
- **Phân bố**:
  - Fiction: 575 books
  - History: 74 books  
  - Drama: 28 books
  - Poetry: 21 books
  - Travel: 14 books
  - Biography: 14 books
  - Và các loại khác...

## ⚠️ Lưu ý

1. **Test trước**: Luôn chạy `test` trước khi upload `full`
2. **Backup**: Đảm bảo có backup dữ liệu Firebase
3. **Rate limiting**: Script có delay 1s giữa các batch
4. **Categories**: Cần tạo categories trước khi upload books

## 🔧 Tùy chỉnh

### Thay đổi batch size
Sửa trong `uploadToFirebase.js`:
```javascript
await uploadBooksToFirebase(filePath, 100, false) // 100 books/batch
```

### Thay đổi category mapping
Sửa trong `convertBookData.js`:
```javascript
const genreMapping = {
  'Fiction': 1,
  'Drama': 2,
  // ... thêm categories
}
```

## 🎯 Kết quả

Sau khi upload thành công, bạn có thể:
- Sử dụng API `/api/books` để lấy danh sách
- Tìm kiếm theo tên, tác giả, keywords
- Lọc theo category
- Phân trang kết quả
