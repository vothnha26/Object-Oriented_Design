package com.alotra.service.order;

import com.alotra.entity.Address;
import com.alotra.entity.Customer;
import com.alotra.entity.Order;
import com.alotra.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderFacade {
    private final CheckoutService checkoutService;
    private final OrderHistoryService orderHistoryService;
    private final VendorOrderService vendorOrderService;

    public OrderFacade(CheckoutService checkoutService,
                       OrderHistoryService orderHistoryService,
                       VendorOrderService vendorOrderService) {
        this.checkoutService = checkoutService;
        this.orderHistoryService = orderHistoryService;
        this.vendorOrderService = vendorOrderService;
    }

    public Order placeOrder(Customer customer, Address address, List<OrderItem> items, String paymentMethod, String note) {
        return checkoutService.createOrder(customer, address, items, paymentMethod, note);
    }

    public List<Order> getCustomerOrderHistory(Integer customerId) {
        return orderHistoryService.findByCustomer(customerId);
    }

    public List<Order> getVendorOrders(Integer vendorId) {
        return vendorOrderService.findByVendor(vendorId);
    }
}
