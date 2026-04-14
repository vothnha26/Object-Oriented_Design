# 📖 Hướng Dẫn Điền Credentials Sepay - Chi Tiết Từng Bước

## ⚡ TL;DR (Nhanh nhất)

1. Chạy ngrok: `ngrok http 8080` → copy URL
2. Đăng ký Sepay: https://dashboard.sandbox.sepay.vn/
3. Lấy API Key, Secret, Webhook Secret từ Dashboard
4. Điền vào `.env` file
5. Chạy Spring Boot: `mvn spring-boot:run`

---

## 📋 Bước 1: Chạy Ngrok (Tạo Public URL)

### 1.1. Download Ngrok (nếu chưa có)

- Vào: https://ngrok.com/download
- Download theo hệ điều hành của bạn (Windows, Mac, Linux)

### 1.2. Extract Ngrok

```bash
# Windows: Giải nén file .zip
# Bạn sẽ nhận được file: ngrok.exe

# macOS:
unzip ~/Downloads/ngrok-v3-stable-darwin-amd64.zip
```

### 1.3. Mở PowerShell/Terminal tại thư mục ngrok

**Windows (PowerShell):**

```powershell
# Điều hướng đến thư mục chứa ngrok.exe
cd C:\Users\YourUsername\Downloads\ngrok  # (hoặc nơi bạn extract)

# Xem ngrok có ở đây không
ls ngrok.exe

# Chạy ngrok
./ngrok http 8080
```

**OUTPUT:** Bạn sẽ thấy:

```
ngrok                                       (Ctrl+C to quit)

Session Status                online
Account                       your-email@example.com
Version                       3.3.5
Region                        us,sg,in (global load balanced reverse proxy)
Latency                       x ms
Web Interface                 http://127.0.0.1:4040
Forwarding                    https://xxxx-xxxx-xxxx.ngrok-free.dev -> http://localhost:8080

Connections                   ttl     opn     rt1     rt5     p50     p95
                              0       0       0.00    0.00    0.00    0.00
```

### 1.4. **GHI NHỚ URL**

```
https://xxxx-xxxx-xxxx.ngrok-free.dev
```

(Mỗi lần chạy ngrok sẽ khác, nên ghi nhớ lần này)

### 1.5. Để Terminal này chạy

**❗ Quan trọng:** Đừng đóng terminal này, để nó chạy liên tục

---

## 🔐 Bước 2: Đăng Ký Sepay & Lấy Credentials

### 2.1. Truy cập Sepay Sandbox

- Mở browser: https://dashboard.sandbox.sepay.vn/
- Hoặc Google: "Sepay sandbox"

### 2.2. Đăng Ký Tài Khoản

1. Click **"Đăng ký"** / **"Sign Up"**
2. Nhập:
   - **Email**: your-email@example.com
   - **Mật khẩu**: Mạnh (chữ + số + ký tự đặc biệt)
   - **Số điện thoại**: 09xxxxxxxxx
   - **Tên cửa hàng**: ALOTRA Coffee
   - **Loại hình**: Nhà hàng / Quán cà phê
3. Click **"Đăng ký"**
4. **Xác thực Email**: Sepay gửi link → click vào link

### 2.3. Vào Dashboard

- Sau khi xác thực, login với email + password
- Bạn sẽ thấy Dashboard Sepay

### 2.4. Tìm & Copy API Key

**Path:** Dashboard → **Settings** (hoặc ⚙️ icon) → **API Keys**

Tìm 2 thông tin:

```
📋 API Key (hoặc Publish Key):
   Format: sk_sandbox_abc123def456...
   → Copy thằng này vào Notepad

📋 API Secret (hoặc Client Secret):
   Format: ss_sandbox_xyz789uvw456...
   → Copy thằng này vào Notepad
```

**Tên khác có thể:**

- "Publish Key" = API Key
- "Secret Key" = API Secret
- "Client ID" = API Key
- "Client Secret" = API Secret

### 2.5. Tìm & Copy Webhook Secret

**Path:** Dashboard → **Settings** → **Webhooks**

1. Chọn **"Add Webhook"** hoặc **"Create Webhook"**
2. Điền:
   - **Webhook URL**: `https://xxxx-xxxx-xxxx.ngrok-free.dev/api/payments/sepay/callback`
     (URL từ bước 1.4)
   - **Events**: Chọn "Payment Completed", "Payment Failed"
3. Click **"Create"** / **"Save"**
4. Sepay sẽ hiển thị **Webhook Secret**:
   ```
   wh_sandbox_secret_here_xxxxxxx
   ```
   → Copy vào Notepad

### 2.6. Tìm API Base URL

**Path:** Dashboard → **Docs** (hoặc Documentation)

Tìm:

```
API Base URL (Sandbox):
https://api.sandbox.sepay.vn/

hoặc

https://sandbox-api.sepay.vn/
```

Ghi lại URL này.

---

## 📝 Bước 3: Điền File `.env`

### 3.1. Mở file `.env`

```
d:\ThietKePhanMemOOP\Project2\Object-Oriented_Design\.env
```

(Hoặc: Chuột phải Project → mở file `.env` bằng Text Editor)

### 3.2. Kiểm tra nội dung hiện tại

File sẽ trông như thế này:

```bash
# 1. API Key
SEPAY_API_KEY=sk_sandbox_xxxxxxxxxxxxxxxxx

# 2. API Secret
SEPAY_SECRET=ss_sandbox_xxxxxxxxxxxxxxxxx

# 3. Webhook Secret
SEPAY_WEBHOOK_SECRET=wh_sandbox_xxxxxxxxxxxxxxxxx

# API Base URL
SEPAY_API_BASE=https://api.sandbox.sepay.vn/

# Public URLs
PUBLIC_BASE_URL=https://replace_with_your_ngrok_url
RETURN_URL=https://replace_with_your_ngrok_url/payment/return
```

### 3.3. Thay Thế Giá Trị (Copy-Paste từ Notepad)

**Thay:**

```bash
SEPAY_API_KEY=sk_sandbox_xxxxxxxxxxxxxxxxx
```

**Thành:**

```bash
SEPAY_API_KEY=sk_sandbox_abc123def456ghi789
```

(Giá trị thực từ Sepay Dashboard)

**Làm tương tự cho:**

- `SEPAY_SECRET` → giá trị từ Dashboard
- `SEPAY_WEBHOOK_SECRET` → giá trị từ Dashboard
- `PUBLIC_BASE_URL` → URL ngrok (từ bước 1.4)
- `RETURN_URL` → URL ngrok + `/payment/return`

### 3.4. Ví Dụ Completed `.env`

```bash
# 1. API Key (từ Dashboard → API Keys)
SEPAY_API_KEY=sk_sandbox_abc123def456ghi789

# 2. API Secret (từ Dashboard → API Keys)
SEPAY_SECRET=ss_sandbox_xyz789uvw456rst123

# 3. Webhook Secret (từ Dashboard → Webhooks)
SEPAY_WEBHOOK_SECRET=wh_sandbox_webhook_secret_here

# API Base URL (Sepay Sandbox)
SEPAY_API_BASE=https://api.sandbox.sepay.vn/

# Public URLs (ngrok URL từ bước 1)
PUBLIC_BASE_URL=https://1234-5678-abcd-efgh.ngrok-free.dev
RETURN_URL=https://1234-5678-abcd-efgh.ngrok-free.dev/payment/return

# JWT & Other (không thay đổi)
APP_SECURITY_ENABLED=true
APPLICATION_SECURITY_JWT_SECRET_KEY=your_jwt_secret_key_replace_with_random_string
APPLICATION_SECURITY_JWT_EXPIRATION=86400000
APPLICATION_SECURITY_JWT_REFRESH_EXPIRATION=604800000
```

### 3.5. Lưu File `.env`

- Ctrl + S (hoặc File → Save)

---

## ✅ Bước 4: Cấu Hình Webhook URL trong Sepay Dashboard

### 4.1. Vào Sepay Dashboard

- Dashboard → **Settings** → **Webhooks**

### 4.2. Cập Nhật Webhook URL

1. Tìm webhook bạn vừa tạo
2. Click **"Edit"** hoặc **"Modify"**
3. Thay đổi **Webhook URL** thành:
   ```
   https://1234-5678-abcd-efgh.ngrok-free.dev/api/payments/sepay/callback
   ```
   (URL ngrok của bạn)
4. Click **"Save"**

### 4.3. Note Webhook Secret

- Sao chép lại **Webhook Secret** nếu cần (vào `.env`)

---

## 🚀 Bước 5: Chạy Spring Boot & Test

### 5.1. Mở Terminal mới (không dùng terminal ngrok)

```powershell
cd d:\ThietKePhanMemOOP\Project2\Object-Oriented_Design

# Chạy Spring Boot
mvn spring-boot:run
```

### 5.2. Kiểm Tra Logs

Tìm trong logs:

```
Sepay config initialized: apiKey=sk_sandbox_abc123...
Loading properties from .env
```

### 5.3. Test Health Endpoint

Mở browser:

```
http://localhost:8080/api/payments/health
```

Kết quả mong đợi:

```json
{ "status": "OK" }
```

### 5.4. Test Webhook (tùy chọn)

```bash
curl -X POST http://localhost:8080/api/payments/sepay/callback \
  -H "Content-Type: application/json" \
  -H "X-Sepay-Signature: test-sig" \
  -d '{
    "orderId": "123",
    "transactionId": "SEP-123",
    "status": "00",
    "amount": 100000
  }'
```

Kết quả:

```json
{ "status": "OK" }
```

---

## 🔍 Troubleshooting

### ❌ "Ngrok không hoạt động"

✅ Kiểm tra:

- Ngrok.exe đang chạy ở terminal?
- Nổi dạng URL: `https://xxxx.ngrok-free.dev`?
- Terminal ngrok không bị đóng?

### ❌ "Sepay credentials không hợp lệ"

✅ Kiểm tra:

- Copy đúng từ Dashboard?
- Không có space trước/sau?
- Dùng sandbox (sk*sandbox*, ss*sandbox*)?
- Đúng cú pháp trong `.env`?

### ❌ ".env không load"

✅ Giải pháp:

- Đóng Spring Boot (Ctrl+C)
- Lưu file `.env` (Ctrl+S)
- Chạy lại: `mvn spring-boot:run`

### ❌ "Webhook URL không nhận"

✅ Kiểm tra:

- Ngrok URL có đổi?
- Webhook URL trong Dashboard có chính xác?
- Terminal ngrok vẫn chạy?
- Xem logs ngrok: http://localhost:4040

---

## 📊 Quy Trình Overview

```
┌─── Ngrok chạy (terminal 1) ───┐
│ ngrok http 8080               │
│ URL: https://xxxx.ngrok.io    │
└─────────────────────────────────┘
        ↓ (copy URL)
┌─── Ngrok URL → .env ───┐
│ PUBLIC_BASE_URL=...     │
│ RETURN_URL=...          │
└─────────────────────────┘
        ↓ (lấy credentials)
┌─── Sepay Dashboard ───────────┐
│ API Key → SEPAY_API_KEY       │
│ API Secret → SEPAY_SECRET     │
│ Webhook Secret → ...          │
└───────────────────────────────┘
        ↓ (điền .env)
┌─── .env file ──────────────┐
│ SEPAY_API_KEY=sk_sand...   │
│ SEPAY_SECRET=ss_sand...    │
│ SEPAY_WEBHOOK_SECRET=...   │
│ PUBLIC_BASE_URL=https://... │
└────────────────────────────┘
        ↓ (save)
┌─── Spring Boot (terminal 2) ───┐
│ mvn spring-boot:run            │
│ Load .env → Sepay ready!       │
└────────────────────────────────┘
```

---

## ✨ Hoàn Thành!

Sau khi làm hết các bước:
✅ Ngrok chạy (terminal 1)
✅ `.env` điền đủ credentials
✅ Webhook URL cấu hình trong Sepay
✅ Spring Boot chạy (terminal 2)
✅ `/api/payments/health` trả OK

**Tiếp theo:** Xây dựng frontend checkout page! 🎉

---

**Cần giúp?**

- Xem docs/SEPAY_INTEGRATION.md (technical reference)
- Xem Sepay docs (nếu có): docs.sepay.vn
- Check logs: ngrok → http://localhost:4040
