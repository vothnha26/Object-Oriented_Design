# 🔴 Bài toán 5: Tính giá sản phẩm cứng nhắc

## Patterns: **Decorator** + **Strategy**
## SOLID vi phạm: **OCP**, **SRP**

---

## 📌 Vấn đề hiện tại

Giá sản phẩm trong đơn hàng được tính qua nhiều bước nhưng code **không tách rời**, không dễ mở rộng:

**`CheckoutService.addItemWithOptions()`** — logic tính giá inline:
```java
// 1. Lấy giá gốc từ variant
BigDecimal basePrice = variant.getPrice();

// 2. Áp dụng khuyến mãi
Integer discountPercent = promotionRepo.findActiveMaxDiscountPercentForProduct(variant.getProduct().getId());
BigDecimal unitPrice = applyPercent(basePrice, discountPercent);

// 3. Tính topping
BigDecimal toppingTotal = BigDecimal.ZERO;
for (Map.Entry<Integer, Integer> entry : toppingSelections.entrySet()) {
    Topping topping = toppingRepo.findById(entry.getKey()).orElse(null);
    toppingTotal = toppingTotal.add(topping.getExtraPrice().multiply(BigDecimal.valueOf(entry.getValue())));
}

// 4. Tổng = (unitPrice + toppingTotal) × quantity
BigDecimal lineTotal = unitPrice.add(toppingTotal).multiply(BigDecimal.valueOf(quantity));
```

### ❌ Vấn đề
1. Logic tính giá **gắn chặt** vào `CheckoutService` → không tái sử dụng khi hiển thị giá trên trang sản phẩm
2. Thêm loại giảm giá mới (VD: giảm giá thành viên VIP, mua 2 tặng 1, giảm cố định cho đơn hàng) → phải **sửa** method → **vi phạm OCP**
3. Thứ tự áp dụng giảm giá không linh hoạt (luôn cố định: KM% → topping → qty)

---

## ✅ Giải pháp: Decorator Pattern

Mỗi "lớp bọc" giá là một Decorator, có thể xếp chồng linh hoạt:

```java
// ===== Component Interface =====
public interface PriceComponent {
    BigDecimal calculate();
    String getDescription();
}

// ===== Base Component =====
public class BasePrice implements PriceComponent {
    private final BigDecimal price;
    
    public BasePrice(BigDecimal price) {
        this.price = price;
    }
    
    @Override public BigDecimal calculate() { return price; }
    @Override public String getDescription() { return "Giá gốc"; }
}

// ===== Abstract Decorator =====
public abstract class PriceDecorator implements PriceComponent {
    protected final PriceComponent wrapped;
    
    protected PriceDecorator(PriceComponent wrapped) {
        this.wrapped = wrapped;
    }
}

// ===== Concrete Decorators =====
public class PromotionDecorator extends PriceDecorator {
    private final int percent;
    
    public PromotionDecorator(PriceComponent wrapped, int percent) {
        super(wrapped);
        this.percent = percent;
    }
    
    @Override
    public BigDecimal calculate() {
        BigDecimal base = wrapped.calculate();
        BigDecimal factor = BigDecimal.valueOf(100 - percent)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return base.multiply(factor).setScale(0, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getDescription() {
        return wrapped.getDescription() + " → Giảm " + percent + "%";
    }
}

public class ToppingDecorator extends PriceDecorator {
    private final List<OrderedTopping> toppings;
    
    public ToppingDecorator(PriceComponent wrapped, List<OrderedTopping> toppings) {
        super(wrapped);
        this.toppings = toppings;
    }
    
    @Override
    public BigDecimal calculate() {
        BigDecimal base = wrapped.calculate();
        BigDecimal extra = toppings.stream()
                .map(t -> t.getPrice().multiply(BigDecimal.valueOf(t.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return base.add(extra);
    }
    
    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + Topping";
    }
}

public class QuantityDecorator extends PriceDecorator {
    private final int quantity;
    
    public QuantityDecorator(PriceComponent wrapped, int quantity) {
        super(wrapped);
        this.quantity = quantity;
    }
    
    @Override
    public BigDecimal calculate() {
        return wrapped.calculate().multiply(BigDecimal.valueOf(quantity));
    }
    
    @Override
    public String getDescription() {
        return wrapped.getDescription() + " × " + quantity;
    }
}

// ===== Decorator mới trong tương lai (OCP!) =====
public class VipDiscountDecorator extends PriceDecorator {
    @Override
    public BigDecimal calculate() {
        return wrapped.calculate().multiply(BigDecimal.valueOf(0.95)); // VIP giảm 5%
    }
}
```

### Sử dụng:

```java
// Trước: logic tính giá inline 20+ dòng
BigDecimal unitPrice = applyPercent(basePrice, discountPercent);
BigDecimal lineTotal = unitPrice.add(toppingTotal).multiply(BigDecimal.valueOf(quantity));

// Sau: chuỗi decorator rõ ràng, dễ mở rộng
PriceComponent price = new BasePrice(variant.getPrice());

if (discountPercent != null && discountPercent > 0) {
    price = new PromotionDecorator(price, discountPercent);
}
if (!toppings.isEmpty()) {
    price = new ToppingDecorator(price, toppings);
}
price = new QuantityDecorator(price, quantity);

BigDecimal lineTotal = price.calculate();
// VD kết quả: "Giá gốc → Giảm 20% + Topping × 2"
```

### Lợi ích

| Trước | Sau |
|-------|-----|
| Tính giá inline trong CheckoutService | Decorator chain tái sử dụng |
| Thêm giảm giá VIP → sửa CheckoutService | Thêm `VipDiscountDecorator` (OCP) |
| Không debug được từng bước giá | `getDescription()` cho biết từng lớp |
| Thứ tự cố định | Xếp decorator theo thứ tự tùy ý |
