# 🎯 Sepay Payment Integration - Complete Setup

Hướng dẫn đầy đủ để triển khai thanh toán Sepay (tiền mặt + chuyển khoản bancông).

## 📋 Nội dung

### ✅ Đã hoàn thành

**1. Backend Core**

- [x] `PaymentStrategy` interface + implementations (Cash, Sepay)
- [x] `SepayPaymentProcessor` — xử lý API, webhook, signature verification
- [x] `PaymentReposit

ory` JPA + DTOs (PaymentRequest, PaymentResult, SepayCallbackDTO)

- [x] REST Controllers — initiate payment + webhook callback
- [x] Configuration — read from `application.properties` via `SepayConfig`
- [x] Database entities — Payment (with PENDING, PAID, FAILED status)

**2. Configuration**

- [x] `application.properties` — sepay.\* properties (template)
- [x] `.env.example` — environment variables template (commit to repo)
- [x] `.env.local` — local development file (do NOT commit)
- [x] `.gitignore` — ignore .env files

**3. Documentation**

- [x] `SEPAY_INTEGRATION.md` — full technical guide
- [x] `SEPAY_SETUP_GUIDE.md` — step-by-step setup for Sepay account
- [x] `SEPAY_QUICKSTART.md` — quick start (5 minutes)

---

## 🚀 Bước tiếp theo: Triển khai

### Phase 1: Cấu hình (hoje)

**1. Đăng ký Sepay**

- Truy cập: https://dashboard.sandbox.sepay.vn/
- Hoặc xem: `docs/SEPAY_SETUP_GUIDE.md` → Bước 1️⃣-2️⃣

**2. Lấy Credentials**

- API Key, Secret, Merchant ID, Webhook Secret
- Xem: `docs/SEPAY_SETUP_GUIDE.md` → Bước 3️⃣-4️⃣

**3. Chạy Ngrok**

```bash
ngrok http 8080
# Copy URL: https://xxxx-xxxx.ngrok-free.dev
```

**4. Điền `.env`**

```bash
# Mở file: .env
# Thay thế:
SEPAY_API_KEY=<your-api-key>
SEPAY_SECRET=<your-secret>
SEPAY_MERCHANT_ID=<your-merchant-id>
SEPAY_WEBHOOK_SECRET=<your-webhook-secret>
PUBLIC_BASE_URL=https://xxxx-xxxx.ngrok-free.dev
```

**5. Chạy Spring Boot**

```bash
mvn spring-boot:run
```

### Phase 2: Testing (tomorrow/next)

- [ ] Test endpoint: `GET /api/payments/health` → OK
- [ ] Simulate Sepay webhook callback
- [ ] Verify signature verification logic
- [ ] Test payment status updates

### Phase 3: Frontend Integration (next)

- [ ] Template checkout page (select Cash vs Sepay)
- [ ] Form submit → POST `/payment/{id}/initiate-sepay`
- [ ] Redirect to Sepay hosted page (or show QR)
- [ ] Poll `/payment/{id}/status` → show success/error
- [ ] Order status update flow

### Phase 4: Production (final)

- [ ] Switch to production credentials (Sepay)
- [ ] Change `PUBLIC_BASE_URL` to real domain (not ngrok)
- [ ] Test with production Sepay API
- [ ] Set up monitoring & logging
- [ ] Create runbooks for incident response

---

## 📁 File Reference

### Configuration Files

```
.env              ← Your local credentials (gitignored)
.env.example      ← Template (commit to repo)
application.properties ← Spring config with env var refs
```

### Documentation

```
docs/SEPAY_INTEGRATION.md    ← Full technical guide
docs/SEPAY_SETUP_GUIDE.md    ← Step-by-step setup
docs/SEPAY_QUICKSTART.md     ← Quick references
```

### Source Code

```
src/main/java/com/alotra/
├── payment/
│   ├── PaymentStrategy.java
│   ├── CashPaymentStrategy.java
│   ├── SepayPaymentStrategy.java
│   ├── PaymentStrategyFactory.java
│   └── SepayPaymentProcessor.java
├── controller/
│   ├── PaymentWebhookController.java
│   └── account/PaymentController.java
├── config/
│   └── SepayConfig.java
├── repository/
│   └── PaymentRepository.java
├── entity/
│   └── Payment.java
└── dto/
    ├── PaymentRequest.java
    ├── PaymentResult.java
    └── SepayCallbackDTO.java

src/main/resources/
└── application.properties (sepay.* properties)
```

---

## 🔑 Key Credentials to Get

| Key                | Where to Find                   | Format | Example             |
| ------------------ | ------------------------------- | ------ | ------------------- |
| **API Key**        | Dashboard → Settings → API Keys | string | `sk_sandbox_abc123` |
| **Secret**         | Dashboard → Settings → API Keys | string | `ss_sandbox_xyz789` |
| **Merchant ID**    | Dashboard → Merchant Info       | string | `M123456`           |
| **Webhook Secret** | Dashboard → Settings → Webhook  | string | `wh_sandbox_secret` |

**Don't share these!** They're secrets — keep in `.env` (gitignored).

---

## 🧪 Quick Test

After `.env.local` is filled and Spring Boot running:

```bash
# 1. Health check
curl http://localhost:8080/api/payments/health

# 2. Simulate webhook callback
curl -X POST http://localhost:8080/api/payments/sepay/callback \
  -H "Content-Type: application/json" \
  -H "X-Sepay-Signature: test" \
  -d '{"merchantId":"M123","orderId":"1","status":"00","amount":100000}'

# 3. View ngrok requests
open http://localhost:4040
```

---

## ❓ FAQ

**Q: Tôi chưa biết lấy credentials ở đâu?**
A: Xem `docs/SEPAY_SETUP_GUIDE.md` → Bước 3️⃣-4️⃣

**Q: Làm sao load `.env.local`?**
A: IDE tự load (nếu cài "DotEnv" extension) hoặc xem `docs/SEPAY_INTEGRATION.md` → Section 1.3

**Q: Ngrok URL thay đổi mỗi lần, phải làm sao?**
A: Cập nhật `PUBLIC_BASE_URL` trong `.env.local` mỗi lần chạy ngrok

**Q: Webhook không hoạt động?**
A: Kiểm tra:

1. Ngrok chạy: `ngrok http 8080`
2. Webhook URL đúng ở Dashboard
3. Xem logs: `http://localhost:4040`
4. Đọc: `docs/SEPAY_INTEGRATION.md` → Troubleshooting

**Q: Giá trị nào không được commit?**
A: Tất cả `.env*` files (except `.env.example`)

- Xem `.gitignore` để verify

---

## 📞 Support

**For Sepay issues:**

- Email: support@sepay.vn
- Dashboard chat

**For code issues:**

- Check logs: `~/logs/sepay.log`
- xem: `docs/SEPAY_INTEGRATION.md` → Troubleshooting

---

## 🎬 Next Steps

1. **Today**: Follow `docs/SEPAY_SETUP_GUIDE.md` → get credentials
2. **Today**: Fill `.env.local` → test Spring Boot
3. **Tomorrow**: Build frontend (checkout page)
4. **This week**: End-to-end testing
5. **Next week**: Production deployment

---

**Starting?** → Read `docs/SEPAY_QUICKSTART.md` (5 min overview)

**Detailed?** → Read `docs/SEPAY_SETUP_GUIDE.md` (step-by-step)

**Technical?** → Read `docs/SEPAY_INTEGRATION.md` (full reference)

---

**Last Updated**: 2026-04-12  
**Status**: ✅ Backend complete, 🔄 Awaiting frontend + testing
