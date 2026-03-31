package com.alotra.service;

import com.alotra.entity.*;
import com.alotra.entity.enums.CartStatus;
import com.alotra.entity.enums.PaymentMethod;
import com.alotra.entity.enums.ReceivingMethod;
import com.alotra.repository.*;
import com.alotra.service.pricing.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SelectedToppingRepository selectedToppingRepository;
    private final ToppingRepository toppingRepository;
    private final OrderedToppingRepository orderedToppingRepository;
    private final AppliedPromotionRepository appliedPromotionRepository;

    public CartService(CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductVariantRepository variantRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            SelectedToppingRepository selectedToppingRepository,
            ToppingRepository toppingRepository,
            OrderedToppingRepository orderedToppingRepository,
            AppliedPromotionRepository appliedPromotionRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.variantRepository = variantRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.selectedToppingRepository = selectedToppingRepository;
        this.toppingRepository = toppingRepository;
        this.orderedToppingRepository = orderedToppingRepository;
        this.appliedPromotionRepository = appliedPromotionRepository;
    }

    @Transactional
    public Cart getOrCreateActiveCart(Customer customer) {
        return cartRepository.findFirstByCustomerAndStatus(customer, CartStatus.ACTIVE).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setCustomer(customer);
            cart.setStatus(CartStatus.ACTIVE);
            return cartRepository.save(cart);
        });
    }

    @Transactional
    public CartItem addItemWithOptions(Customer customer, Integer variantId, int qty, Map<Integer, Integer> toppingQty,
            String note) {
        if (qty <= 0)
            qty = 1;
        Cart cart = getOrCreateActiveCart(customer);
        ProductVariant variant = variantRepository.findById(variantId).orElse(null);
        if (variant == null)
            throw new IllegalArgumentException("Biến thể không hợp lệ.");

        // 1. Resolve Toppings
        Map<Topping, Integer> toppingMap = new HashMap<>();
        if (toppingQty != null) {
            for (Map.Entry<Integer, Integer> entry : toppingQty.entrySet()) {
                Integer perUnitQty = entry.getValue();
                if (perUnitQty == null || perUnitQty <= 0)
                    continue;
                Topping topping = toppingRepository.findById(entry.getKey()).orElse(null);
                if (topping != null)
                    toppingMap.put(topping, perUnitQty);
            }
        }

        // 2. Assemble Price Decorators
        PriceComponent priceComponent = new BasePrice(variant.getPrice());
        Integer discountPercent = (variant.getProduct() != null)
                ? appliedPromotionRepository.findActiveMaxDiscountPercentForProduct(variant.getProduct().getId())
                : null;
        if (discountPercent != null && discountPercent > 0) {
            priceComponent = new PromotionDecorator(priceComponent, discountPercent);
        }

        // 3. Set Unit Price (Base + Promo)
        BigDecimal unitPrice = priceComponent.calculate();

        if (!toppingMap.isEmpty()) {
            priceComponent = new ToppingDecorator(priceComponent, toppingMap);
        }
        priceComponent = new QuantityDecorator(priceComponent, qty);

        // Create a fresh cart item
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setVariant(variant);
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        item.setNote(note);
        item.setLineTotal(priceComponent.calculate());
        item = cartItemRepository.save(item);

        // Persist topping items
        if (toppingQty != null) {
            for (Map.Entry<Integer, Integer> entry : toppingQty.entrySet()) {
                Integer tid = entry.getKey();
                Integer perUnitQty = entry.getValue();
                if (perUnitQty == null || perUnitQty <= 0)
                    continue;
                Topping topping = toppingRepository.findById(tid).orElse(null);
                if (topping == null)
                    continue;

                SelectedTopping cit = new SelectedTopping();
                cit.setCartItem(item);
                cit.setTopping(topping);
                cit.setQuantity(perUnitQty * qty);
                cit.setUnitPrice(topping.getExtraPrice());
                cit.setLineTotal(topping.getExtraPrice().multiply(BigDecimal.valueOf(cit.getQuantity())));
                selectedToppingRepository.save(cit);
            }
        }

        // Log detailed price calculation via Decorator
        System.out.println("[Cart Pricing Strategy] " + priceComponent.getDescription() + " = " + item.getLineTotal());
        return item;
    }

    public List<CartItem> listItems(Customer customer) {
        Cart cart = cartRepository.findFirstByCustomerAndStatus(customer, CartStatus.ACTIVE).orElse(null);
        if (cart == null)
            return List.of();
        return cartItemRepository.findByCart(cart);
    }

    public int getItemCount(Customer customer) {
        try {
            return listItems(customer).stream().mapToInt(CartItem::getQuantity).sum();
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public void updateToppings(Customer customer, Integer itemId, Map<Integer, Integer> toppingQtyById) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow();
        validateOwnership(customer, item);

        // 1. Clear existing toppings for a clean slate
        selectedToppingRepository.deleteByCartItem(item);
        selectedToppingRepository.flush();

        // 2. Add new/restored toppings
        if (toppingQtyById != null) {
            for (Map.Entry<Integer, Integer> entry : toppingQtyById.entrySet()) {
                Integer tid = entry.getKey();
                Integer qty = entry.getValue() == null ? 0 : Math.max(0, entry.getValue());
                if (qty <= 0) continue;

                Topping topping = toppingRepository.findById(tid).orElse(null);
                if (topping != null) {
                    SelectedTopping cit = new SelectedTopping();
                    cit.setCartItem(item);
                    cit.setTopping(topping);
                    cit.setQuantity(qty * item.getQuantity());
                    cit.setUnitPrice(topping.getExtraPrice());
                    cit.setLineTotal(topping.getExtraPrice().multiply(BigDecimal.valueOf(cit.getQuantity())));
                    selectedToppingRepository.save(cit);
                }
            }
        }
        selectedToppingRepository.flush();

        // 3. Recalculate everything
        recomputeLineTotal(item);
        cartItemRepository.flush();
    }

    @Transactional
    public void updateQuantity(Customer customer, Integer itemId, int qty) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow();
        validateOwnership(customer, item);
        if (qty <= 0) {
            cartItemRepository.delete(item);
            return;
        }
        int oldQty = item.getQuantity();
        item.setQuantity(qty);
        
        // Sync topping totals
        List<SelectedTopping> selected = selectedToppingRepository.findByCartItem(item);
        for (SelectedTopping st : selected) {
            int qtyPerItem = st.getQuantity() / oldQty;
            st.setQuantity(qtyPerItem * qty);
            st.setLineTotal(st.getUnitPrice().multiply(BigDecimal.valueOf(st.getQuantity())));
            selectedToppingRepository.save(st);
        }
        
        recomputeLineTotal(item);
    }

    @Transactional
    public void removeItem(Customer customer, Integer itemId) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow();
        validateOwnership(customer, item);
        cartItemRepository.delete(item);
    }

    private void validateOwnership(Customer customer, CartItem item) {
        if (!java.util.Objects.equals(item.getCart().getCustomer().getId(), customer.getId())) {
            throw new SecurityException("Không có quyền với mục giỏ hàng này");
        }
    }

    public BigDecimal calcTotal(List<CartItem> items) {
        BigDecimal total = items.stream().map(CartItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("[CartService] Calculated total for " + items.size() + " items: " + total);
        return total;
    }

    @Transactional
    public Order checkoutWithOptions(Customer customer, List<Integer> itemIds, String paymentMethod,
            String note, String receivingMethod,
            String shipName, String shipPhone, String shipAddress) {
        if (itemIds == null || itemIds.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn sản phẩm để đặt hàng");
        }
        Cart activeCart = getOrCreateActiveCart(customer);
        List<CartItem> items = cartItemRepository.findAllById(new HashSet<>(itemIds)).stream()
                .filter(it -> it.getCart() != null
                        && java.util.Objects.equals(it.getCart().getId(), activeCart.getId()))
                .collect(Collectors.toList());
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Không có sản phẩm hợp lệ để đặt hàng");
        }

        // 3. Create Order using OrderBuilder (Builder Pattern)
        Order order = com.alotra.builder.OrderBuilder.builder()
                .forCustomer(customer)
                .payBy(paymentMethod)
                .receivingMethod(receivingMethod)
                .shipTo(shipName, shipPhone, shipAddress)
                .withNote(note)
                .withSubtotal(calcTotal(items))
                .withDiscount(BigDecimal.ZERO)
                .withShippingFee(BigDecimal.ZERO)
                .build();

        order = orderRepository.save(order);

        // 4. Persist lines and toppings
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
            activeCart.setStatus(CartStatus.CHECKED_OUT);
            cartRepository.save(activeCart);
            getOrCreateActiveCart(customer);
        }
        return order;
    }

    public List<Topping> listActiveToppings() {
        return toppingRepository.findByDeletedAtIsNull();
    }

    public Map<Integer, List<SelectedTopping>> getToppingsForItems(List<CartItem> items) {
        Map<Integer, List<SelectedTopping>> map = new HashMap<>();
        for (CartItem it : items) {
            map.put(it.getId(), selectedToppingRepository.findByCartItem(it));
        }
        return map;
    }


    private void recomputeLineTotal(CartItem item) {
        // 1. Core unit price (Base + Promo)
        BigDecimal basePrice = item.getVariant().getPrice();
        Integer discountPercent = appliedPromotionRepository.findActiveMaxDiscountPercentForProduct(item.getVariant().getProduct().getId());
        PriceComponent unitComp = new BasePrice(basePrice);
        if (discountPercent != null && discountPercent > 0) {
            unitComp = new PromotionDecorator(unitComp, discountPercent);
        }
        item.setUnitPrice(unitComp.calculate());

        // 2. Sync SelectedToppings and build Map for Decorator
        List<SelectedTopping> selected = selectedToppingRepository.findByCartItem(item);
        Map<Topping, Integer> toppingMap = new HashMap<>();
        int itemQty = Math.max(1, item.getQuantity());

        if (selected != null) {
            for (SelectedTopping st : selected) {
                // Ensure record consistency
                st.setLineTotal(st.getUnitPrice().multiply(BigDecimal.valueOf(st.getQuantity())));
                selectedToppingRepository.save(st);
                
                // Derive unit quantity for Decorator logic
                int perItem = st.getQuantity() / itemQty;
                if (perItem > 0) {
                    toppingMap.put(st.getTopping(), perItem);
                }
            }
        }

        // 3. Chain Decorators for full line total
        PriceComponent priceComponent = new BasePrice(item.getUnitPrice());
        if (!toppingMap.isEmpty()) {
            priceComponent = new ToppingDecorator(priceComponent, toppingMap);
        }
        priceComponent = new QuantityDecorator(priceComponent, itemQty);
        
        item.setLineTotal(priceComponent.calculate());
        cartItemRepository.save(item);
        
        System.out.println("[Cart Recompute] Item=" + item.getId() + " Qty=" + itemQty + " Total=" + item.getLineTotal());
    }

    @Transactional
    public void changeVariant(Customer customer, Integer itemId, Integer newVariantId) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow();
        validateOwnership(customer, item);
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
        PriceComponent priceComponent = new BasePrice(target.getPrice());
        if (discountPercent != null && discountPercent > 0) {
            priceComponent = new PromotionDecorator(priceComponent, discountPercent);
        }

        item.setVariant(target);
        item.setUnitPrice(priceComponent.calculate());
        recomputeLineTotal(item);
    }

    public Map<Integer, Integer> getCurrentToppingQtys(Integer itemId) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow();
        List<SelectedTopping> selected = selectedToppingRepository.findByCartItem(item);
        return selected.stream().collect(Collectors.toMap(
            t -> t.getTopping().getId(), 
            t -> t.getQuantity() / item.getQuantity()
        ));
    }

    public List<ProductVariant> listVariantsForProduct(Product product) {
        if (product == null)
            return List.of();
        return variantRepository.findByProduct(product);
    }
}
