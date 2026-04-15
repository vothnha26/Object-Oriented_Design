package com.alotra.service.order;

import com.alotra.dto.CartItemDTO;
import com.alotra.entity.Customer;
import com.alotra.entity.Order;
import com.alotra.entity.OrderItem;
import com.alotra.entity.ProductVariant;
import com.alotra.entity.OrderedTopping;
import com.alotra.entity.Topping;
import com.alotra.repository.ProductVariantRepository;
import com.alotra.repository.ToppingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderFactory {

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ToppingRepository toppingRepository;

    public Order createOrder(Customer customer, List<CartItemDTO> items, String note) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemDTO dto : items) {
            ProductVariant variant = variantRepository.findById(dto.getVariantId()).orElseThrow();

            OrderItem item = new OrderItem();
            item.setVariant(variant);
            item.setQuantity(dto.getQuantity());
            item.setUnitPrice(variant.getPrice());
            item.setNote(dto.getNote());

            if (dto.getToppingIds() != null) {
                for (Integer tId : dto.getToppingIds()) {
                    Topping topping = toppingRepository.findById(tId).orElseThrow();
                    OrderedTopping ot = new OrderedTopping();
                    ot.setTopping(topping);
                    ot.setPrice(topping.getExtraPrice());
                    ot.setQuantity(1);
                    item.getToppings().add(ot);
                    ot.setOrderItem(item);
                }
            }
            orderItems.add(item);
        }

        return com.alotra.builder.OrderBuilder.builder()
                .forCustomer(customer)
                .withItems(orderItems)
                .build();
    }
}
