package com.alotra.service.query;

import com.alotra.entity.Order;
import com.alotra.repository.OrderRepository;
import java.util.List;
import java.util.stream.Collectors;

public class KeywordFilter extends AbstractOrderQuery {
    public KeywordFilter(OrderRepository repository) {
        super(repository);
    }

    @Override
    public List<Order> execute(Object kw, Object list) {
        String keyword = (String) kw;
        List<Order> orders = (List<Order>) list;
        if (keyword == null || keyword.isBlank()) return orders;
        
        String lower = keyword.toLowerCase();
        return orders.stream()
                .filter(order -> 
                    (order.getId().toString().contains(keyword)) ||
                    (order.getCustomer().getFullName() != null && order.getCustomer().getFullName().toLowerCase().contains(lower)) ||
                    (order.getShippingAddressLine() != null && order.getShippingAddressLine().toLowerCase().contains(lower))
                )
                .collect(Collectors.toList());
    }
}
