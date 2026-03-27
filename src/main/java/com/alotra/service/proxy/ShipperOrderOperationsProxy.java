package com.alotra.service.proxy;

import com.alotra.entity.Order;
import com.alotra.repository.OrderRepository;
import com.alotra.service.ShipperOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Primary
public class ShipperOrderOperationsProxy implements ShipperOrderOperations {
    private static final Logger log = LoggerFactory.getLogger(ShipperOrderOperationsProxy.class);

    private final ShipperOrderOperations real;
    private final OrderRepository orderRepository;

    public ShipperOrderOperationsProxy(@Qualifier("shipperOrderOperationsReal") ShipperOrderOperations real,
                                       OrderRepository orderRepository) {
        this.real = real;
        this.orderRepository = orderRepository;
    }

    @Override
    public Map<String, Object> getDashboardStats(Integer shipperId) {
        return real.getDashboardStats(shipperId);
    }

    @Override
    public List<ShipperOrderService.OrderDto> getAssignedOrders(Integer shipperId, String status, String keyword, Integer limit) {
        return real.getAssignedOrders(shipperId, status, keyword, limit);
    }

    @Override
    public List<ShipperOrderService.OrderDto> getAvailableOrders(String keyword, Integer limit) {
        return real.getAvailableOrders(keyword, limit);
    }

    @Override
    public List<ShipperOrderService.OrderDto> getTodayShippingOrders(Integer shipperId) {
        return real.getTodayShippingOrders(shipperId);
    }

    @Override
    public boolean markAsDelivered(Integer orderId, Integer shipperId) {
        if (!validateOwnership(orderId, shipperId, "markAsDelivered")) {
            return false;
        }
        return real.markAsDelivered(orderId, shipperId);
    }

    @Override
    public boolean acceptOrder(Integer orderId, Integer shipperId) {
        return real.acceptOrder(orderId, shipperId);
    }

    @Override
    public boolean advanceOrder(Integer orderId, Integer shipperId) {
        if (!validateOwnership(orderId, shipperId, "advanceOrder")) {
            return false;
        }
        return real.advanceOrder(orderId, shipperId);
    }

    @Override
    public boolean advanceOrderSimple(Integer orderId, Integer shipperId) {
        return real.advanceOrderSimple(orderId, shipperId);
    }

    @Override
    public boolean cancelOrder(Integer orderId, Integer shipperId) {
        if (!validateOwnership(orderId, shipperId, "cancelOrder")) {
            return false;
        }
        return real.cancelOrder(orderId, shipperId);
    }

    @Override
    public boolean isOrderAssignedToShipper(Integer orderId, Integer shipperId) {
        if (!validateOwnership(orderId, shipperId, "isOrderAssignedToShipper")) {
            return false;
        }
        return real.isOrderAssignedToShipper(orderId, shipperId);
    }

    @Override
    public boolean confirmPayment(Integer orderId, Integer shipperId) {
        if (!validateOwnership(orderId, shipperId, "confirmPayment")) {
            return false;
        }
        return real.confirmPayment(orderId, shipperId);
    }

    private boolean validateOwnership(Integer orderId, Integer shipperId, String action) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        Integer ownerId = order.getEmployee() == null ? null : order.getEmployee().getId();
        if (!Objects.equals(ownerId, shipperId)) {
            log.warn("SECURITY: shipper {} attempted {} on order {} owned by {}",
                    shipperId, action, orderId, ownerId);
            return false;
        }
        return true;
    }
}