package com.alotra.service.order;

import com.alotra.dto.CartItemDTO;
import com.alotra.entity.*;
import com.alotra.repository.ProductVariantRepository;
import com.alotra.repository.ToppingRepository;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderFactory {
    private final ProductVariantRepository variantRepository;
    private final ToppingRepository toppingRepository;

    public OrderFactory(ProductVariantRepository variantRepository, 
                        ToppingRepository toppingRepository) {
        this.variantRepository = variantRepository;
        this.toppingRepository = toppingRepository;
    }

    public Order createOrder(Customer customer, Address address, List<CartItemDTO> cartItems, String note) {
        Order order = new Order();
        order.setCustomer(customer);
        if (address != null) {
            order.setShippingAddressLine(address.getAddressLine());
        }
        
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItemDTO itemDto : cartItems) {
            OrderItem orderItem = createOrderItem(itemDto);
            orderItem.setOrder(order);
            // Ghi chú được gán vào từng OrderItem theo thiết kế PUML
            orderItem.setNote(note);
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);
        return order;
    }

    private OrderItem createOrderItem(CartItemDTO dto) {
        ProductVariant variant = variantRepository.findById(dto.getVariantId())
                .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + dto.getVariantId()));

        OrderItem orderItem = new OrderItem();
        orderItem.setVariant(variant);
        orderItem.setQuantity(dto.getQuantity());
        orderItem.setUnitPrice(variant.getPrice());

        if (dto.getToppingIds() != null) {
            List<OrderedTopping> toppings = new ArrayList<>();
            for (Integer toppingId : dto.getToppingIds()) {
                Topping topping = toppingRepository.findById(toppingId)
                        .orElseThrow(() -> new IllegalArgumentException("Topping not found: " + toppingId));
                
                OrderedTopping orderedTopping = new OrderedTopping();
                orderedTopping.setTopping(topping);
                orderedTopping.setPrice(topping.getExtraPrice());
                orderedTopping.setOrderItem(orderItem);
                toppings.add(orderedTopping);
            }
            orderItem.setToppings(toppings);
        }
        return orderItem;
    }
}
