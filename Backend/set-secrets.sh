#!/bin/bash

# Script để set tất cả biến môi trường từ file .env lên Fly.io
# Sử dụng: bash set-secrets.sh

set -e

ENV_FILE=".env"
APP_NAME="readingbook-api-cold-hill-3738"

# Kiểm tra file .env có tồn tại không
if [ ! -f "$ENV_FILE" ]; then
    echo "❌ File $ENV_FILE không tồn tại!"
    echo "💡 Tạo file .env từ env.example: cp env.example .env"
    exit 1
fi

echo "📖 Đang đọc file $ENV_FILE..."
echo ""

# Kiểm tra flyctl
if ! command -v flyctl &> /dev/null; then
    echo "❌ flyctl chưa được cài đặt!"
    exit 1
fi

# Kiểm tra đã đăng nhập chưa
if ! flyctl auth whoami &> /dev/null; then
    echo "⚠️  Chưa đăng nhập Fly.io"
    flyctl auth login
fi

# Đếm số biến sẽ được set
TOTAL=$(grep -v '^#' "$ENV_FILE" | grep -v '^$' | grep '=' | wc -l)
echo "🔍 Tìm thấy $TOTAL biến môi trường"
echo ""

# Xác nhận
read -p "❓ Bạn có muốn set tất cả biến này lên Fly.io? (y/n): " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Đã hủy"
    exit 0
fi

echo ""
echo "🚀 Bắt đầu set secrets..."
echo ""

# Đếm số biến đã set thành công
COUNT=0
FAILED=0

# Đọc file .env và set từng biến
while IFS= read -r line || [ -n "$line" ]; do
    # Bỏ qua comment và dòng trống
    if [[ "$line" =~ ^[[:space:]]*# ]] || [[ -z "${line// }" ]]; then
        continue
    fi

    # Kiểm tra có dấu = không
    if [[ "$line" =~ = ]]; then
        # Tách key và value
        KEY=$(echo "$line" | cut -d'=' -f1 | xargs)
        VALUE=$(echo "$line" | cut -d'=' -f2- | xargs)

        # Bỏ quotes nếu có
        VALUE="${VALUE#\"}"
        VALUE="${VALUE%\"}"
        VALUE="${VALUE#\'}"
        VALUE="${VALUE%\'}"

        # Bỏ qua nếu key hoặc value rỗng
        if [ -z "$KEY" ] || [ -z "$VALUE" ]; then
            continue
        fi

        # Bỏ qua các giá trị placeholder
        if [[ "$VALUE" =~ ^your- ]] || [[ "$VALUE" == "your-project-id" ]] || [[ "$VALUE" == "your-super-secret-jwt-key-here" ]]; then
            echo "⏭️  Bỏ qua $KEY (chưa được cấu hình)"
            continue
        fi

        # Set secret
        echo -n "⚙️  Đang set $KEY... "
        if flyctl secrets set "${KEY}=${VALUE}" &> /dev/null; then
            echo "✅"
            ((COUNT++))
        else
            echo "❌"
            ((FAILED++))
        fi
    fi
done < "$ENV_FILE"

echo ""
echo "✅ Hoàn thành!"
echo "📊 Thống kê:"
echo "   - Đã set: $COUNT biến"
echo "   - Thất bại: $FAILED biến"
echo ""
echo "🔍 Xem tất cả secrets: flyctl secrets list"
echo ""

