package com.alotra.service.order;

import com.alotra.entity.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderFacade {
    private final OrderHistoryService orderHistoryService;
    private final VendorOrderService vendorOrderService;

    public OrderFacade(OrderHistoryService orderHistoryService,
                       VendorOrderService vendorOrderService) {
        this.orderHistoryService = orderHistoryService;
        this.vendorOrderService = vendorOrderService;
    }

    public List<Order> getCustomerOrderHistory(Integer customerId) {
        return orderHistoryService.findByCustomer(customerId);
    }

    public List<Order> getVendorOrders(Integer vendorId) {
        return vendorOrderService.findByVendor(vendorId);
    }
}
