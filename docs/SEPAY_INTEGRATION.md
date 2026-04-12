# Sepay Payment Integration - Developer Guide

## 1. Local Development Setup với Ngrok

### 1.1. Tải & chạy Ngrok

```bash
# Download từ https://ngrok.com/download
# Sau khi extract, chạy:
./ngrok http 8080

# Output sẽ hiển thị:
# Forwarding    https://xxxx-xx-xx-xxx-xx.ngrok-free.dev -> http://localhost:8080
# Copy URL này (public HTTPS URL)
```

### 1.2. Cấu hình `.env` file

Tạo/chỉnh sửa file `.env` ở root project:

```bash
# .env file
SEPAY_API_KEY=your_sandbox_api_key
SEPAY_SECRET=your_sandbox_secret
SEPAY_MERCHANT_ID=your_merchant_id
SEPAY_WEBHOOK_SECRET=your_webhook_secret
SEPAY_API_BASE=https://api.sandbox.sepay.vn/
SEPAY_MODE=sandbox
PUBLIC_BASE_URL=https://xxxx-xx-xx-xxx-xx.ngrok-free.dev
RETURN_URL=https://xxxx-xx-xx-xxx-xx.ngrok-free.dev/payment/return
```

### 1.3. Load `.env` vào Spring Boot

Option 1: Dùng IDE (Intellij / VS Code + Extension)

- Cài extension "DotEnv" hoặc tương tự
- IDE sẽ tự load biến từ `.env` vào environment

Option 2: Chạy qua Maven/Gradle với env vars:

```bash
export $(cat .env | xargs)
mvn spring-boot:run
```

Option 3: Chạy trực tiếp Java với system properties:

```bash
java -Dsepay.apiKey=... -Dsepay.secret=... -jar target/app.jar
```

## 2. Sepay Sandbox Credentials

Cần lấy từ Sepay:

- **API Key** — authentication key for API calls
- **Secret** — HMAC signing secret
- **Merchant ID** — unique identifier for your merchant account
- **Webhook Secret** — signature verification key for callbacks
- **Sandbox Endpoint** — https://api.sandbox.sepay.vn/ (or production)

Đặt các thông tin này vào `.env` file, **KHÔNG commit** vào repo.

## 3. Project Structure

```
src/main/java/com/alotra/
├── payment/
│   ├── PaymentStrategy.java              # Interface (CASH, SEPAY, BANK_TRANSFER)
│   ├── CashPaymentStrategy.java          # Thanh toán tiền mặt (COD)
│   ├── SepayPaymentStrategy.java         # Strategy cho Sepay (online)
│   ├── PaymentStrategyFactory.java       # Factory pattern -> create strategy
│   └── SepayPaymentProcessor.java        # Core Sepay logic (API, webhook, signature)
├── controller/
│   ├── PaymentWebhookController.java     # webhook endpoint: POST /api/payments/sepay/callback
│   └── account/PaymentController.java    # initiate endpoint: POST /payment/{id}/initiate-sepay
├── config/
│   └── SepayConfig.java                  # Read sepay.* properties from application.properties
├── repository/
│   └── PaymentRepository.java            # DB access for Payment entity
└── dto/
    ├── PaymentRequest.java
    ├── PaymentResult.java
    └── SepayCallbackDTO.java
```

## 4. Workflow: Khách hàng thanh toán với Sepay

1. **Frontend**: Khách hàng chọn "thanh toán qua Sepay" ở checkout
2. **POST /payment/{orderId}/initiate-sepay** → Controller calls SepayPaymentProcessor
3. **SepayPaymentProcessor.initiateSepayPayment()** → tạo Payment record (status=PENDING), sinh redirect URL
4. **Frontend**: Redirect user đến Sepay hosted page (hoặc hiển thị QR code)
5. **Sepay**: Khách hàng chọn ngân hàng, nhập thông tin, thanh toán
6. **Sepay**: Send webhook POST → **http://your-public-url/api/payments/sepay/callback**
7. **PaymentWebhookController.handleSepayCallback()** → verify signature, update Payment (status=PAID)
8. **Frontend**: Poll `/payment/{id}/status` để kiểm tra trạng thái → redirect success page

## 5. Testing Webhook Locally

### 5.1. Kiểm tra Webhook URL từ Sepay

Sepay sẽ gọi: `POST https://your-ngrok-url/api/payments/sepay/callback`

Để test, bạn có thể:

A) Dùng **curl** để simulate webhook call (local):

```bash
curl -X POST http://localhost:8080/api/payments/sepay/callback \
  -H "Content-Type: application/json" \
  -H "X-Sepay-Signature: your-computed-signature" \
  -d '{
    "merchantId": "your-merchant-id",
    "orderId": "123",
    "transactionId": "SEP-123456",
    "status": "00",
    "amount": 100000
  }'
```

B) Dùng **Postman**:

- Import collection: `postman/sepay-sandbox.postman_collection.json`
- Set variables: merchantId, orderId, transactionId
- Compute signature bằng Postman pre-request script
- POST to `http://localhost:8080/api/payments/sepay/callback`

C) Dùng **ngrok web dashboard**:

- Mở http://localhost:4040 (ngrok local UI)
- Xem real-time requests, headers, body
- Replay requests để test

### 5.2. Signature Verification Logic

```java
// SepayPaymentProcessor.generateHmacSignature() sử dụng:
// Mac.getInstance("HmacSHA256")
// Message = JSON payload or custom string
// Secret = sepay.webhookSecret
// Signature = Base64(HMAC-SHA256(message, secret))

// Verify tương tự:
// receivedSignature == computedSignature ? proceed : reject
```

## 6. Running Tests

### Compile project:

```bash
mvn clean compile
```

### Run existing tests:

```bash
mvn test
```

### Unit test SepayPaymentProcessor (example structure):

```java
@Test
public void testSepaySignatureVerification() {
    String message = "payment-data";
    String secret = "test-secret";
    String signature = processor.generateHmacSignature(message, secret);
    assertTrue(processor.verifyWebhookSignature(message, signature));
}
```

## 7. Production Deployment

### Checklist:

- [ ] Copy `.env.example` → `.env` (fill real Sepay credentials)
- [ ] Set `sepay.mode=production` in `.env`
- [ ] Change `sepay.apiBaseUrl` to production endpoint from Sepay
- [ ] Deploy to server with PUBLIC_BASE_URL = real domain (not ngrok)
- [ ] Test with Sepay sandbox first, then production
- [ ] Monitor logs: check webhook callbacks are received & verified
- [ ] Set up alerts for payment failures
- [ ] Document incident response (if webhook fails)

## 8. Troubleshooting

### Issue: "Webhook not received"

- ✓ Check ngrok is running: `ngrok http 8080`
- ✓ Verify PUBLIC_BASE_URL matches ngrok URL in .env
- ✓ Confirm Sepay dashboard has correct callback URL
- ✓ Check firewall/NAT doesn't block incoming connections

### Issue: "Signature verification failed"

- ✓ Verify SEPAY_WEBHOOK_SECRET is correct
- ✓ Check payload format matches expected format (JSON vs form-data)
- ✓ Ensure HMAC-SHA256 algorithm used (not SHA1, MD5)
- ✓ Check timestamp is fresh (not too old)

### Issue: "Order not found after webhook"

- ✓ Verify orderId in webhook payload matches actual order ID
- ✓ Check database: is Payment entity saved correctly?
- ✓ Review logs for SepayPaymentProcessor.handleSepayCallback()

## 9. API Endpoints Reference

| Method | Endpoint                     | Auth              | Purpose                |
| ------ | ---------------------------- | ----------------- | ---------------------- |
| POST   | /payment/{id}/initiate-sepay | Customer          | Initiate Sepay payment |
| GET    | /payment/{id}/status         | Customer          | Check payment status   |
| POST   | /api/payments/sepay/callback | None (verify sig) | Webhook from Sepay     |
| GET    | /api/payments/health         | None              | Health check           |

## 10. Security Notes

1. **Never commit secrets** (apiKey, secret, webhookSecret) → use .env + .gitignore
2. **Always verify webhook signature** before trusting callback
3. **HTTPS only** in production (ngrok provides HTTPS by default)
4. **Rate limit** webhook endpoint to prevent replay attacks
5. **Log webhook payloads** (masked) for debugging
6. **Monitor** for failed payment callbacks → retry logic if needed

## 11. References

- Sepay API Docs: https://docs.sepay.vn/ (if available)
- Spring Boot @Value & environment variables: https://spring.io/blog/2011/02/15/externalized-configuration-in-spring
- HMAC-SHA256 in Java: `javax.crypto.Mac`
- Ngrok: https://ngrok.com

---

**Last Updated**: 2026-04-12
**Author**: Payment Integration Team
