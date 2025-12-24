# Script để set tất cả biến môi trường từ file .env lên Fly.io (PowerShell)
# Sử dụng: .\set-secrets.ps1

$ErrorActionPreference = "Stop"

$envFile = ".env"
$appName = "readingbook-api-cold-hill-3738"

# Kiểm tra file .env
if (-not (Test-Path $envFile)) {
    Write-Host "❌ File $envFile không tồn tại!" -ForegroundColor Red
    Write-Host "💡 Tạo file .env từ env.example: Copy-Item env.example .env" -ForegroundColor Yellow
    exit 1
}

Write-Host "📖 Đang đọc file $envFile..." -ForegroundColor Cyan
Write-Host ""

# Kiểm tra flyctl
try {
    $null = flyctl version 2>$null
} catch {
    Write-Host "❌ flyctl chưa được cài đặt!" -ForegroundColor Red
    exit 1
}

# Kiểm tra đã đăng nhập chưa
try {
    $null = flyctl auth whoami 2>$null
} catch {
    Write-Host "⚠️  Chưa đăng nhập Fly.io" -ForegroundColor Yellow
    flyctl auth login
}

# Đếm số biến
$total = (Get-Content $envFile | Where-Object { $_ -notmatch '^\s*#' -and $_ -match '=' -and $_ -notmatch '^\s*$' }).Count
Write-Host "🔍 Tìm thấy $total biến môi trường" -ForegroundColor Cyan
Write-Host ""

# Xác nhận
$confirm = Read-Host "❓ Bạn có muốn set tất cả biến này lên Fly.io? (y/n)"
if ($confirm -ne "y" -and $confirm -ne "Y") {
    Write-Host "❌ Đã hủy" -ForegroundColor Red
    exit 0
}

Write-Host ""
Write-Host "🚀 Bắt đầu set secrets..." -ForegroundColor Cyan
Write-Host ""

$count = 0
$failed = 0

# Đọc file .env
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()

    # Bỏ qua comment và dòng trống
    if ($line -match '^\s*#' -or $line -eq "") {
        return
    }

    # Kiểm tra có dấu = không
    if ($line -match '=') {
        $parts = $line -split '=', 2
        $key = $parts[0].Trim()
        $value = $parts[1].Trim()

        # Bỏ quotes nếu có
        if ($value.StartsWith('"') -or $value.StartsWith("'")) {
            $value = $value.Substring(1)
        }
        if ($value.EndsWith('"') -or $value.EndsWith("'")) {
            $value = $value.Substring(0, $value.Length - 1)
        }

        # Bỏ qua nếu key hoặc value rỗng
        if ([string]::IsNullOrWhiteSpace($key) -or [string]::IsNullOrWhiteSpace($value)) {
            return
        }

        # Bỏ qua các giá trị placeholder
        if ($value -match '^your-' -or $value -eq "your-project-id" -or $value -eq "your-super-secret-jwt-key-here") {
            Write-Host "⏭️  Bỏ qua $key (chưa được cấu hình)" -ForegroundColor Gray
            return
        }

        # Set secret
        Write-Host -NoNewline "⚙️  Đang set $key... " -ForegroundColor Cyan
        try {
            $secretValue = "${key}=${value}"
            flyctl secrets set $secretValue --app $appName | Out-Null
            if ($LASTEXITCODE -eq 0) {
                Write-Host "✅" -ForegroundColor Green
                $count++
            } else {
                Write-Host "❌" -ForegroundColor Red
                $failed++
            }
        } catch {
            Write-Host "❌" -ForegroundColor Red
            $failed++
        }
    }
}

Write-Host ""
Write-Host "✅ Hoàn thành!" -ForegroundColor Green
Write-Host "📊 Thống kê:" -ForegroundColor Cyan
Write-Host "   - Đã set: $count biến" -ForegroundColor Green
Write-Host "   - Thất bại: $failed biến" -ForegroundColor $(if ($failed -gt 0) { "Red" } else { "Green" })
Write-Host ""
Write-Host "🔍 Xem tất cả secrets: flyctl secrets list" -ForegroundColor Cyan
Write-Host ""

