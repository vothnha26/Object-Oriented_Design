package com.alotra.service;

import com.alotra.entity.*;
import com.alotra.entity.enums.PaymentMethod;
import com.alotra.entity.enums.ReceivingMethod;
import com.alotra.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for handling checkout operations.
 * Extracted from CartService to achieve Single Responsibility Principle.
 * Responsible for: order creation, payment processing, shipping info, persisting order data.
 */
@Service
public class CheckoutService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderedToppingRepository orderedToppingRepository;
    private final SelectedToppingRepository selectedToppingRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    public CheckoutService(OrderRepository orderRepository,
                          OrderItemRepository orderItemRepository,
                          OrderedToppingRepository orderedToppingRepository,
                          SelectedToppingRepository selectedToppingRepository,
                          CartItemRepository cartItemRepository,
                          CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderedToppingRepository = orderedToppingRepository;
        this.selectedToppingRepository = selectedToppingRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
    }

    /**
     * Process checkout for selected cart items.
     * Creates order, persists order items with toppings, clears cart items.
     */
    @Transactional
    public Order checkoutWithOptions(Customer customer, Cart activeCart, List<Integer> itemIds,
                                     String paymentMethod, String note, String receivingMethod,
                                     String shipName, String shipPhone, String shipAddress) {
        if (itemIds == null || itemIds.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn sản phẩm để đặt hàng");
        }

        // Get selected items from cart
        List<CartItem> items = cartItemRepository.findAllById(new HashSet<>(itemIds)).stream()
            .filter(it -> it.getCart() != null && Objects.equals(it.getCart().getId(), activeCart.getId()))
            .collect(Collectors.toList());

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Không có sản phẩm hợp lệ để đặt hàng");
        }

        // Create order
        Order order = new Order();
        order.setCustomer(customer);

        // Set payment method
        Payment payment = new Payment();
        if (paymentMethod != null) {
            try {
                payment.setMethod(PaymentMethod.valueOf(paymentMethod.toUpperCase()));
            } catch (Exception ignored) {
            }
        }
        order.setPayment(payment);

        // Build shipping info
        ShippingInfo shipping = buildShippingInfo(customer, note, receivingMethod, shipName, shipPhone, shipAddress);
        order.setShippingInfo(shipping);

        // Compute totals
        BigDecimal subtotal = calculateTotal(items);
        order.setSubtotal(subtotal);
        order.setDiscount(BigDecimal.ZERO);
        order.setShippingFee(BigDecimal.ZERO);
        order.setTotalAmount(subtotal.add(order.getShippingFee()).subtract(order.getDiscount()));

        order = orderRepository.save(order);

        // Persist order items with toppings
        persistOrderItems(order, items);

        // Clean up cart
        cleanupCartItems(activeCart, items);

        return order;
    }

    /**
     * Calculate total price for cart items.
     */
    public BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
            .map(CartItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get toppings grouped by cart item.
     */
    public Map<Integer, List<SelectedTopping>> getToppingsForItems(List<CartItem> items) {
        Map<Integer, List<SelectedTopping>> map = new HashMap<>();
        for (CartItem item : items) {
            List<SelectedTopping> toppings = selectedToppingRepository.findByCartItem(item);
            map.put(item.getId(), toppings);
        }
        return map;
    }

    // Private helper methods

    private ShippingInfo buildShippingInfo(Customer customer, String note, String receivingMethod,
                                          String shipName, String shipPhone, String shipAddress) {
        ShippingInfo shipping = new ShippingInfo();
        StringBuilder orderNote = new StringBuilder();

        if (note != null && !note.isBlank()) {
            orderNote.append(note.trim());
        }

        boolean isDelivery = "Ship".equalsIgnoreCase(receivingMethod);
        if (isDelivery) {
            shipping.setMethod(ReceivingMethod.DELIVERY);

            String recvName = (shipName != null && !shipName.isBlank()) ? shipName.trim()
                : (customer.getFullName() != null ? customer.getFullName().trim() : null);
            String recvPhone = (shipPhone != null && !shipPhone.isBlank()) ? shipPhone.trim()
                : (customer.getPhone() != null ? customer.getPhone().trim() : null);
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

        return shipping;
    }

    private void persistOrderItems(Order order, List<CartItem> cartItems) {
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setVariant(ci.getVariant());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getUnitPrice());
            oi.setLineTotal(ci.getLineTotal());
            oi.setNote(ci.getNote());
            oi = orderItemRepository.save(oi);

            // Persist toppings for this order item
            List<SelectedTopping> selectedToppings = selectedToppingRepository.findByCartItem(ci);
            for (SelectedTopping cit : selectedToppings) {
                OrderedTopping oit = new OrderedTopping();
                oit.setOrderLine(oi);
                oit.setTopping(cit.getTopping());
                oit.setQuantity(cit.getQuantity());
                oit.setUnitPrice(cit.getUnitPrice());
                oit.setLineTotal(cit.getLineTotal());
                orderedToppingRepository.save(oit);
            }
        }
    }

    private void cleanupCartItems(Cart activeCart, List<CartItem> checkedOutItems) {
        // Delete toppings and items
        for (CartItem ci : checkedOutItems) {
            List<SelectedTopping> toppings = selectedToppingRepository.findByCartItem(ci);
            for (SelectedTopping cit : toppings) {
                selectedToppingRepository.delete(cit);
            }
            cartItemRepository.delete(ci);
        }

        // If cart is empty, mark as checked out and create new active cart
        boolean noMoreItems = cartItemRepository.findByCart(activeCart).isEmpty();
        if (noMoreItems) {
            activeCart.setStatus(com.alotra.entity.enums.CartStatus.CHECKED_OUT);
            cartRepository.save(activeCart);
        }
    }
}
