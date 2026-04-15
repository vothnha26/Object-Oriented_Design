package com.alotra.service.query;

import com.alotra.entity.Order;

public class KeywordFilter implements OrderFilterStrategy {
    private final String keyword;

    public KeywordFilter(String keyword) {
        this.keyword = keyword != null ? keyword.toLowerCase() : "";
    }

    @Override
    public boolean matches(Order order) {
        if (keyword.isEmpty()) return true;
        
        String idStr = order.getId() != null ? order.getId().toString() : "";
        String customerName = order.getCustomer() != null ? order.getCustomer().getFullName().toLowerCase() : "";
        
        return idStr.contains(keyword) || customerName.contains(keyword);
    }
}
