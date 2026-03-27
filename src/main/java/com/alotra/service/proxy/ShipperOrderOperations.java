package com.alotra.service.proxy;

import com.alotra.service.ShipperOrderService;

import java.util.List;
import java.util.Map;

public interface ShipperOrderOperations {
    Map<String, Object> getDashboardStats(Integer shipperId);

    List<ShipperOrderService.OrderDto> getAssignedOrders(Integer shipperId, String status, String keyword, Integer limit);

    List<ShipperOrderService.OrderDto> getAvailableOrders(String keyword, Integer limit);

    List<ShipperOrderService.OrderDto> getTodayShippingOrders(Integer shipperId);

    boolean markAsDelivered(Integer orderId, Integer shipperId);

    boolean acceptOrder(Integer orderId, Integer shipperId);

    boolean advanceOrder(Integer orderId, Integer shipperId);

    boolean advanceOrderSimple(Integer orderId, Integer shipperId);

    boolean cancelOrder(Integer orderId, Integer shipperId);

    boolean isOrderAssignedToShipper(Integer orderId, Integer shipperId);

    boolean confirmPayment(Integer orderId, Integer shipperId);
}