package com.alotra.service;

import com.alotra.entity.*;
import com.alotra.entity.enums.PaymentMethod;
import com.alotra.entity.enums.ReceivingMethod;
import com.alotra.repository.*;
import com.alotra.service.proxy.CartOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service("cartOperationsReal")
public class CartService implements CartOperations {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SelectedToppingRepository selectedToppingRepository;
    private final ToppingRepository toppingRepository;
    private final OrderedToppingRepository orderedToppingRepository;
    private final AppliedPromotionRepository appliedPromotionRepository;

    public CartService(CartRepository cartRepository, 
                       CartItemRepository cartItemRepository,
                       ProductVariantRepository variantRepository, 
                       ProductRepository productRepository,
                       OrderRepository orderRepository, 
                       OrderItemRepository orderItemRepository,
                       SelectedToppingRepository selectedToppingRepository, 
                       ToppingRepository toppingRepository,
                       OrderedToppingRepository orderedToppingRepository,
                       AppliedPromotionRepository appliedPromotionRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.selectedToppingRepository = selectedToppingRepository;
        this.toppingRepository = toppingRepository;
        this.orderedToppingRepository = orderedToppingRepository;
        this.appliedPromotionRepository = appliedPromotionRepository;
    }

    @Transactional
    @Override
    public Cart getOrCreateActiveCart(Customer customer) {
        return cartRepository.findFirstByCustomerAndStatus(customer, "ACTIVE").orElseGet(() -> {
            Cart cart = new Cart();
            cart.setCustomer(customer);
            cart.setStatus("ACTIVE");
            return cartRepository.save(cart);
        });
    }

    @Transactional
    @Override
    public CartItem addItemWithOptions(Customer customer, Integer variantId, int qty, Map<Integer, Integer> toppingQty, String note) {
        if (qty <= 0) qty = 1;
        Cart cart = getOrCreateActiveCart(customer);
        ProductVariant variant = variantRepository.findById(variantId).orElse(null);
        if (variant == null) throw new IllegalArgumentException("Biến thể không hợp lệ.");
        
        // Apply active product promotion to base
        BigDecimal basePrice = variant.getPrice();
        Integer discountPercent = (variant.getProduct() != null) ? appliedPromotionRepository.findActiveMaxDiscountPercentForProduct(variant.getProduct().getId()) : null;
        BigDecimal unitPrice = applyPercent(basePrice, discountPercent);
        
        // Create a fresh cart item
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setVariant(variant);
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        item.setNote(note);
        
        // Compute toppings per unit
        BigDecimal toppingPerUnit = BigDecimal.ZERO;
        if (toppingQty != null) {
            for (Map.Entry<Integer, Integer> entry : toppingQty.entrySet()) {
                Integer tid = entry.getKey();
                Integer perUnitQty = entry.getValue();
                if (perUnitQty == null || perUnitQty <= 0) continue;
                Topping topping = toppingRepository.findById(tid).orElse(null);
                if (topping == null) continue;
                toppingPerUnit = toppingPerUnit.add(topping.getExtraPrice().multiply(BigDecimal.valueOf(perUnitQty)));
            }
        }
        
        BigDecimal lineTotal = unitPrice.add(toppingPerUnit).multiply(BigDecimal.valueOf(qty));
        item.setLineTotal(lineTotal);
        item = cartItemRepository.save(item);
        
        // Persist topping items
        if (toppingQty != null) {
            for (Map.Entry<Integer, Integer> entry : toppingQty.entrySet()) {
                Integer tid = entry.getKey();
                Integer perUnitQty = entry.getValue();
                if (perUnitQty == null || perUnitQty <= 0) continue;
                Topping topping = toppingRepository.findById(tid).orElse(null);
                if (topping == null) continue;
                
                SelectedTopping cit = new SelectedTopping();
                cit.setCartItem(item);
                cit.setTopping(topping);
                cit.setQuantity(perUnitQty * qty);
                cit.setUnitPrice(topping.getExtraPrice());
                cit.setLineTotal(topping.getExtraPrice().multiply(BigDecimal.valueOf(cit.getQuantity())));
                selectedToppingRepository.save(cit);
            }
        }
        return item;
    }

    private BigDecimal applyPercent(BigDecimal base, Integer percent) {
        if (base == null) return null;
        if (percent == null || percent <= 0) return base;
        java.math.RoundingMode roundingMode = java.math.RoundingMode.HALF_UP;
        BigDecimal discountMultiplier = BigDecimal.valueOf(100 - Math.min(100, percent))
                .divide(BigDecimal.valueOf(100), 4, roundingMode);
        return base.multiply(discountMultiplier).setScale(0, roundingMode);
    }

    @Override
    public List<CartItem> listItems(Customer customer) {
        Cart cart = cartRepository.findFirstByCustomerAndStatus(customer, "ACTIVE").orElse(null);
        if (cart == null) return List.of();
        return cartItemRepository.findByCart(cart);
    }

    @Override
    public int getItemCount(Customer customer) {
        try {
            return listItems(customer).stream().mapToInt(CartItem::getQuantity).sum();
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    @Override
    public void updateQuantity(Customer customer, Integer itemId, int qty) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow();
        if (qty <= 0) {
            cartItemRepository.delete(item);
            return;
        }
        item.setQuantity(qty);
        recomputeLineTotal(item);
    }

    @Transactional
    @Override
    public void removeItem(Customer customer, Integer itemId) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow();
        cartItemRepository.delete(item);
    }

    @Override
    public BigDecimal calcTotal(List<CartItem> items) {
        return items.stream().map(CartItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    @Override
    public Order checkoutWithOptions(Customer customer, List<Integer> itemIds, String paymentMethod,
                                     String note, String receivingMethod,
                                     String shipName, String shipPhone, String shipAddress) {
        if (itemIds == null || itemIds.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn sản phẩm để đặt hàng");
        }
        Cart activeCart = getOrCreateActiveCart(customer);
        List<CartItem> items = cartItemRepository.findAllById(new HashSet<>(itemIds)).stream()
                .filter(it -> it.getCart() != null && java.util.Objects.equals(it.getCart().getId(), activeCart.getId()))
                .collect(Collectors.toList());
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Không có sản phẩm hợp lệ để đặt hàng");
        }
        
        Order order = new Order();
        order.setCustomer(customer);
        
        Payment payment = new Payment();
        if (paymentMethod != null) {
            try { payment.setMethod(PaymentMethod.valueOf(paymentMethod.toUpperCase())); } catch (Exception ignored) {}
        }
        order.setPayment(payment);
        
        // Build ShippingInfo
        ShippingInfo shipping = new ShippingInfo();
        StringBuilder orderNote = new StringBuilder();
        if (note != null && !note.isBlank()) orderNote.append(note.trim());
        boolean isDelivery = "Ship".equalsIgnoreCase(receivingMethod);
        if (isDelivery) {
            shipping.setMethod(ReceivingMethod.DELIVERY);
            String recvName = (shipName != null && !shipName.isBlank()) ? shipName.trim() : (customer.getFullName() != null ? customer.getFullName().trim() : null);
            String recvPhone = (shipPhone != null && !shipPhone.isBlank()) ? shipPhone.trim() : (customer.getPhone() != null ? customer.getPhone().trim() : null);
            String recvAddr = (shipAddress != null && !shipAddress.isBlank()) ? shipAddress.trim() : null;
            if (recvPhone == null || recvPhone.isBlank() || recvAddr == null || recvAddr.isBlank()) {
                throw new IllegalArgumentException("Vui lòng nhập đầy đủ SĐT và Địa chỉ khi chọn Ship tận nơi");
            }
            shipping.setReceiverName(recvName);
            shipping.setReceiverPhone(recvPhone);
            shipping.setShippingAddress(recvAddr);
            if (orderNote.length() > 0) orderNote.append(" | ");
            orderNote.append("Ship to: ");
            if (recvName != null && !recvName.isBlank()) orderNote.append(recvName).append(", ");
            orderNote.append(recvPhone).append(", ").append(recvAddr);
        } else {
            shipping.setMethod(ReceivingMethod.PICKUP);
        }
        order.setShippingInfo(shipping);
        if (orderNote.length() > 0) order.setNote(orderNote.toString());
        
        // Compute totals
        BigDecimal subtotal = calcTotal(items);
        order.setSubtotal(subtotal);
        order.setDiscount(BigDecimal.ZERO);
        order.setShippingFee(BigDecimal.ZERO);
        order.setTotalAmount(subtotal.add(order.getShippingFee()).subtract(order.getDiscount()));
        order = orderRepository.save(order);
        
        // Persist lines and toppings
        for (CartItem ci : items) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setVariant(ci.getVariant());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getUnitPrice());
            oi.setLineTotal(ci.getLineTotal());
            oi.setNote(ci.getNote());
            oi = orderItemRepository.save(oi);
            
            for (SelectedTopping cit : selectedToppingRepository.findByCartItem(ci)) {
                OrderedTopping oit = new OrderedTopping();
                oit.setOrderLine(oi);
                oit.setTopping(cit.getTopping());
                oit.setQuantity(cit.getQuantity());
                oit.setUnitPrice(cit.getUnitPrice());
                oit.setLineTotal(cit.getLineTotal());
                orderedToppingRepository.save(oit);
            }
        }
        
        // Cleanup cart items
        for (CartItem ci : items) {
            for (SelectedTopping cit : selectedToppingRepository.findByCartItem(ci)) {
                selectedToppingRepository.delete(cit);
            }
            cartItemRepository.delete(ci);
        }
        
        boolean noMoreItems = cartItemRepository.findByCart(activeCart).isEmpty();
        if (noMoreItems) {
            activeCart.setStatus("CHECKED_OUT");
            cartRepository.save(activeCart);
            getOrCreateActiveCart(customer);
        }
        return order;
    }

    @Override
    public List<Topping> listActiveToppings() {
        return toppingRepository.findByDeletedAtIsNull();
    }

    @Override
    public Map<Integer, List<SelectedTopping>> getToppingsForItems(List<CartItem> items) {
        Map<Integer, List<SelectedTopping>> map = new HashMap<>();
        for (CartItem it : items) {
            map.put(it.getId(), selectedToppingRepository.findByCartItem(it));
        }
        return map;
    }

    @Transactional
    @Override
    public void updateToppings(Customer customer, Integer itemId, Map<Integer, Integer> toppingQtyById) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow();
        
        List<SelectedTopping> existing = selectedToppingRepository.findByCartItem(item);
        Map<Integer, SelectedTopping> existingByTid = existing.stream()
                .collect(Collectors.toMap(t -> t.getTopping().getId(), t -> t));
        
        if (toppingQtyById != null) {
            for (Map.Entry<Integer, Integer> entry : toppingQtyById.entrySet()) {
                Integer tid = entry.getKey();
                Integer qty = entry.getValue() == null ? 0 : Math.max(0, entry.getValue());
                Topping topping = toppingRepository.findById(tid).orElse(null);
                if (topping == null) continue;
                
                if (qty == 0) {
                    SelectedTopping exist = existingByTid.get(tid);
                    if (exist != null) selectedToppingRepository.delete(exist);
                } else {
                    SelectedTopping exist = existingByTid.get(tid);
                    if (exist == null) {
                        exist = new SelectedTopping();
                        exist.setCartItem(item);
                        exist.setTopping(topping);
                    }
                    exist.setQuantity(qty);
                    exist.setUnitPrice(topping.getExtraPrice());
                    exist.setLineTotal(topping.getExtraPrice().multiply(BigDecimal.valueOf(qty)));
                    selectedToppingRepository.save(exist);
                }
            }
        }
        recomputeLineTotal(item);
    }

    private void recomputeLineTotal(CartItem item) {
        BigDecimal toppingSum = selectedToppingRepository.findByCartItem(item).stream()
                .map(SelectedTopping::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal baseTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        item.setLineTotal(baseTotal.add(toppingSum));
        cartItemRepository.save(item);
    }

    @Transactional
    @Override
    public void changeVariant(Customer customer, Integer itemId, Integer newVariantId) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow();
        ProductVariant target = variantRepository.findById(newVariantId).orElse(null);
        if (target == null || !target.isActive()) {
            throw new IllegalArgumentException("Biến thể không hợp lệ hoặc đang ngừng bán");
        }
        
        Integer curProductId = item.getVariant().getProduct().getId();
        Integer targetProductId = target.getProduct().getId();
        if (!Objects.equals(curProductId, targetProductId)) {
            throw new IllegalArgumentException("Không thể đổi sang sản phẩm khác");
        }
        
        Integer discountPercent = appliedPromotionRepository.findActiveMaxDiscountPercentForProduct(targetProductId);
        BigDecimal newUnitPrice = applyPercent(target.getPrice(), discountPercent);
        item.setVariant(target);
        item.setUnitPrice(newUnitPrice);
        recomputeLineTotal(item);
    }

    @Override
    public List<ProductVariant> listVariantsForProduct(Product product) {
        if (product == null) return List.of();
        return variantRepository.findByProduct(product);
    }
}
