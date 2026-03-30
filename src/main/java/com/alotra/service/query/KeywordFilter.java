package com.alotra.service.query;

import com.alotra.entity.Order;

public class KeywordFilter implements OrderFilterStrategy {
    private final String keyword;

    public KeywordFilter(String keyword) {
        this.keyword = keyword != null ? keyword.toLowerCase() : "";
    }

    @Override
    public boolean matches(Order order) {
        if (keyword.isBlank()) return true;
        String custName = order.getCustomer() != null && order.getCustomer().getFullName() != null
            ? order.getCustomer().getFullName().toLowerCase() : "";
        String phone = order.getShippingInfo() != null && order.getShippingInfo().getReceiverPhone() != null 
            ? order.getShippingInfo().getReceiverPhone() : "";
        String address = order.getShippingInfo() != null && order.getShippingInfo().getShippingAddress() != null 
            ? order.getShippingInfo().getShippingAddress().toLowerCase() : "";
        return custName.contains(keyword) || phone.contains(keyword) || address.contains(keyword);
    }
}
