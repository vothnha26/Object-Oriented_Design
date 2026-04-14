# Sepay Tài Khoản & Credentials - Hướng Dẫn Chi Tiết

## 📋 Tóm tắt nhanh

| Bước | Chi tiết                | Kết quả                     |
| ---- | ----------------------- | --------------------------- |
| 1    | Đăng ký tài khoản Sepay | Email + Password            |
| 2    | Tạo Merchant account    | Merchant ID                 |
| 3    | Lấy API Key & Secret    | SEPAY_API_KEY, SEPAY_SECRET |
| 4    | Cấu hình Webhook        | SEPAY_WEBHOOK_SECRET        |
| 5    | Chạy ngrok              | ngrok URL                   |
| 6    | Điền `.env` file        | Deployment ready            |

---

## 1️⃣ Đăng ký Tài Khoản Sepay

### 1.1. Truy cập trang Sepay

```
https://dashboard.sandbox.sepay.vn/
hoặc
https://sepay.vn (Production)
```

### 1.2. Click "Đăng ký" / "Sign Up"

- Nhập **Email** (sử dụng để login sau này)
- Nhập **Mật khẩu** (mạnh, có chữ + số + ký tự đặc biệt)
- Nhập **Số điện thoại**
- Chọn **Loại tài khoản**: Merchant (cửa hàng)
- Chấp nhận điều khoản & điều kiện

### 1.3. Xác thực Email

- Sepay gửi email xác thực
- Click link trong email
- Tài khoản kích hoạt

### 1.4. Hoàn thành KYC (Know Your Customer) - nếu có

- Nhập thông tin cá nhân / công ty
- Upload CCCD / CMND
- Đợi phê duyệt (thường vài giờ)

---

## 2️⃣ Tạo Merchant Account

### 2.1. Vào Dashboard

Sau khi đăng ký xong:

```
https://dashboard.sandbox.sepay.vn/merchant/settings
```

### 2.2. Điền thông tin Merchant

- **Tên cửa hàng**: ALOTRA Coffee
- **Website**: https://your-ngrok-url (hoặc your-domain.com)
- **Loại hình**: Nhà hàng / Quán cà phê / Bán lẻ
- **Địa chỉ**: Địa chỉ của cửa hàng
- **Số điện thoại hỗ trợ**: 0xxxxx

### 2.3. Lưu cài đặt

Click **"Save"** → Merchant tạo xong

**Ghi nhớ Merchant ID** (thường hiển thị trên Dashboard)

- Ví dụ: `M123456` hoặc `ALOTRA` hoặc UUID

---

## 3️⃣ Lấy API Key & Secret

### 3.1. Truy cập API Settings

```
Dashboard → Settings → API Keys
hoặc
https://dashboard.sandbox.sepay.vn/settings/api-keys
```

### 3.2. Tìm & Copy giá trị sau:

**API Key (Client ID)**

- Label: "Publish Key" hoặc "API Key" hoặc "Client ID"
- Format: `sk_sandbox_xxx...` hoặc `pk_sandbox_xxx...`
- Copy và dán vào Notepad

**API Secret (Client Secret)**

- Label: "Secret Key" hoặc "Client Secret" hoặc "API Secret"
- Format: `ss_sandbox_xxx...` hoặc `sk_secret_xxx...`
- Copy và dán vào Notepad

### 3.3. Ví dụ:

```
API Key:    sk_sandbox_01e4c5f6g7h8i9j0k1l2m3n4o5p6
Secret:     ss_sandbox_aaabbbcccdddeeefffggghhh
```

---

## 4️⃣ Cấu hình Webhook & Lấy Webhook Secret

### 4.1. Truy cập Webhook Settings

```
Dashboard → Settings → Webhook
hoặc
https://dashboard.sandbox.sepay.vn/settings/webhooks
```

### 4.2. Thiết lập Webhook Endpoint

- **Webhook URL**: Sẽ thêm sau khi có ngrok
- **Events**: Chọn "Payment Completed", "Payment Failed"
- Click **"Add Webhook"**

### 4.3. Copy Webhook Secret

- Sepay hiển thị "Webhook Secret" (dùng để verify signature)
- Format: `wh_sandbox_xxx...`
- Copy vào Notepad

**Ghi lại:**

```
SEPAY_WEBHOOK_SECRET=wh_sandbox_xxxxyyyzzz
```

---

## 5️⃣ Chạy Ngrok (Local Development)

### 5.1. Download Ngrok

```bash
# Option 1: Download từ https://ngrok.com/download
# Option 2: Dùng package manager (nếu có)
brew install ngrok  # macOS
choco install ngrok  # Windows (chocolatey)
```

### 5.2. Chạy Ngrok

```bash
# Mở terminal/PowerShell và chạy:
ngrok http 8080

# Output:
# Session Status                online
# Account                       your-email@example.com
# Version                       3.xxx
# Region                        us,sg,in (global load balanced reverse proxy)
# Latency                       x ms
# Web Interface                 http://127.0.0.1:4040
# Forwarding                    https://xxxx-xxxx-xxxx.ngrok-free.dev -> http://localhost:8080
```

**Ghi lại ngrok URL:**

```
https://xxxx-xxxx-xxxx.ngrok-free.dev
```

### 5.3. Cập nhật Webhook URL trong Sepay Dashboard

```
Dashboard → Settings → Webhook → Edit
Webhook URL: https://xxxx-xxxx-xxxx.ngrok-free.dev/api/payments/sepay/callback
Save
```

---

## 6️⃣ Điền File `.env`

### 6.1. Mở file `.env` trong project

```
d:\ThietKePhanMemOOP\Project2\Object-Oriented_Design\.env
```

### 6.2. Thay thế các giá trị:

**Trước:**

```bash
SEPAY_API_KEY=sk_sandbox_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
SEPAY_SECRET=ss_sandbox_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
SEPAY_MERCHANT_ID=M123456_REPLACE_WITH_YOURS
SEPAY_WEBHOOK_SECRET=wh_sandbox_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
PUBLIC_BASE_URL=https://replace_with_your_ngrok_url
RETURN_URL=https://replace_with_your_ngrok_url/payment/return
```

**Sau (ví dụ):**

```bash
SEPAY_API_KEY=sk_sandbox_abc123def456ghi789
SEPAY_SECRET=ss_sandbox_xyz789uvw456rst123
SEPAY_MERCHANT_ID=M123456
SEPAY_WEBHOOK_SECRET=wh_sandbox_secret_key_here
PUBLIC_BASE_URL=https://1234-5678-abcd.ngrok-free.dev
RETURN_URL=https://1234-5678-abcd.ngrok-free.dev/payment/return
```

### 6.3. Lưu file `.env`

---

## 7️⃣ Verify cấu hình

### 7.1. Kiểm tra `.env.local` được load

Chạy ứng dụng:

```bash
cd d:\ThietKePhanMemOOP\Project2\Object-Oriented_Design
mvn spring-boot:run
```

Trong logs, tìm:

```
Loading properties from .env.local
Sepay config initialized: merchantId=M123456, apiKey=sk_sandbox_xxx
```

### 7.2. Test webhook endpoint

```bash
# Mở browser:
https://1234-5678-abcd.ngrok-free.dev/api/payments/health

# Kết quả: {"status":"OK"}
```

### 7.3. Check ngrok logs

```
http://localhost:4040
```

Xem request đến ngrok (simulate callback test)

---

## 8️⃣ Troubleshooting

### ❌ "API Key không hợp lệ"

✅ Kiểm tra:

- Copy đúng giá trị từ Dashboard
- Không có space trước/sau
- Đúng sandbox key (không phải production)

### ❌ "Webhook không nhận được"

✅ Kiểm tra:

- Ngrok đang chạy: `ngrok http 8080`
- Webhook URL đúng trong Dashboard
- Firewall không block ngrok
- Xem logs tại `http://localhost:4040`

### ❌ "Signature verification failed"

✅ Kiểm tra:

- SEPAY_WEBHOOK_SECRET trùng giữa `.env` và Dashboard
- Payload format đúng (JSON)
- HMAC-SHA256 được tính đúng

---

## ✅ Checklist Hoàn Thành

- [ ] Đăng ký tài khoản Sepay
- [ ] Tạo Merchant account
- [ ] Lấy API Key & Secret
- [ ] Lấy Webhook Secret
- [ ] Chạy ngrok → copy URL
- [ ] Cập nhật Webhook URL trong Dashboard
- [ ] Điền `.env.local` file
- [ ] Chạy Spring Boot
- [ ] Test `/api/payments/health` endpoint
- [ ] Simulate webhook callback test

---

## 📞 Liên lạc Sepay Support (nếu cần)

- **Email**: support@sepay.vn
- **Chat**: Trên dashboard Sepay
- **Docs**: https://docs.sepay.vn/ (nếu có)

---

**Last Updated**: 2026-04-12
