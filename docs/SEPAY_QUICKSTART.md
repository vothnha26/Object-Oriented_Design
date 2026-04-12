# 🚀 Sepay Payment Integration - Quick Start

## ⚡ Bắt đầu nhanh trong 5 phút

### 1. Sao chép `.env` từ mẫu

```bash
cd d:\ThietKePhanMemOOP\Project2\Object-Oriented_Design

# File .env đã tạo sẵn với template
# Mở .env và thay thế giá trị
```

### 4. Điền File `.env`

**Bạn cần (từ Sepay Dashboard):**

```
SEPAY_API_KEY        → Dashboard → Settings → API Keys
SEPAY_SECRET         → Dashboard → Settings → API Keys
SEPAY_MERCHANT_ID    → Dashboard → Merchant Info
SEPAY_WEBHOOK_SECRET → Dashboard → Settings → Webhook
```

**Hướng dẫn chi tiết:** xem `docs/SEPAY_SETUP_GUIDE.md`

### 3. Chạy Ngrok (local development)

```bash
# Terminal 1: Chạy ngrok
ngrok http 8080

# Copy URL: https://xxxx-xxxx-xxxx.ngrok-free.dev
# Điền vào .env:
# PUBLIC_BASE_URL=https://xxxx-xxxx-xxxx.ngrok-free.dev
```

### 4. Cập nhật Webhook URL trong Sepay Dashboard

```
Dashboard → Settings → Webhook
Webhook URL: https://xxxx-xxxx-xxxx.ngrok-free.dev/api/payments/sepay/callback
Save
```

### 5. Chạy Spring Boot

```bash
# Terminal 2: Chạy Spring Boot
cd d:\ThietKePhanMemOOP\Project2\Object-Oriented_Design
mvn spring-boot:run
```

### 6. Test

```bash
# Mở browser:
http://localhost:8080/api/payments/health

# Kết quả: {"status":"OK"}
```

---

## 📁 File Structure

```
📂 Object-Oriented_Design/
├── .env                        ← Your credentials (do NOT commit)
├── .env.example                ← Template (commit to repo)
├── docs/
│   ├── SEPAY_INTEGRATION.md  ← Full integration guide
│   ├── SEPAY_SETUP_GUIDE.md  ← Step-by-step setup (NEW)
│   └── ...
├── src/main/java/com/alotra/
│   ├── payment/
│   │   ├── SepayPaymentProcessor.java
│   │   ├── SepayPaymentStrategy.java
│   │   ├── CashPaymentStrategy.java
│   │   └── PaymentStrategyFactory.java
│   └── controller/
│       ├── PaymentWebhookController.java
│       └── account/PaymentController.java
└── ...
```

---

## 🔑 Credentials Locations in Sepay Dashboard

| Credential     | Dashboard Path                               |
| -------------- | -------------------------------------------- |
| API Key        | Settings → API Keys → API Key / Publish Key  |
| API Secret     | Settings → API Keys → Secret / Client Secret |
| Merchant ID    | Merchant Info → ID                           |
| Webhook Secret | Settings → Webhook → Secret                  |

---

## 🧪 Test Webhook Locally

### Simulate callback (curl):

```bash
curl -X POST http://localhost:8080/api/payments/sepay/callback \
  -H "Content-Type: application/json" \
  -H "X-Sepay-Signature: test-signature" \
  -d '{
    "merchantId": "M123456",
    "orderId": "123",
    "transactionId": "SEP-123456",
    "status": "00",
    "amount": 100000
  }'
```

### View ngrok logs:

```
http://localhost:4040
```

---

## ✅ Checklist

- [ ] Đăng ký tài khoản Sepay
- [ ] Lấy API Key, Secret, Merchant ID, Webhook Secret
- [ ] Chạy ngrok, copy URL
- [ ] Cập nhật Webhook URL trong Dashboard
- [ ] Điền `ĐỈỀN .env.local` với credentials
- [ ] Chạy Spring Boot
- [ ] Test `/api/payments/health` → OK
- [ ] Test webhook callback
- [ ] Kiểm tra logs

---

## ❓ Câu hỏi thường gặp

**Q: Tôi chưa có tài khoản Sepay**
A: Đọc `docs/SEPAY_SETUP_GUIDE.md` → Bước 1️⃣-2️⃣

**Q: Lấy Merchant ID ở đâu?**
A: Dashboard → Merchant Info hoặc Cài đặt → Thông tin cửa hàng

**Q: Ngrok URL thay đổi mỗi lần chạy**
A: Có, sao chép URL mới vào `PUBLIC_BASE_URL` trong `.env.local`

**Q: Webhook không nhận được**
A: Kiểm tra:

- Ngrok chạy: `ngrok http 8080`
- Webhook URL đúng trong Dashboard
- Xem logs: `http://localhost:4040`

**Q: Giá trị nào bảo mật nhất?**
A: Tất cả (API Key, Secret, Webhook Secret). Không share, không commit.

---

## 📚 Tài liệu

- [Sepay Integration Guide](./SEPAY_INTEGRATION.md)
- [Sepay Setup Step-by-Step](./SEPAY_SETUP_GUIDE.md)
- [Ngrok Documentation](https://ngrok.com/docs)
- [Spring Boot Environment Properties](https://spring.io/blog/2011/02/15/externalized-configuration-in-spring)

---

**Need Help?** → Xem `docs/SEPAY_SETUP_GUIDE.md` để hướng dẫn chi tiết từng bước.
