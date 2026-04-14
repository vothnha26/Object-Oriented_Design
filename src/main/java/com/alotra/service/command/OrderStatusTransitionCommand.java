package com.alotra.service.command;

import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.state.OrderContext;
import com.alotra.repository.OrderRepository;

public class OrderStatusTransitionCommand implements AdminCommand {
    public enum TransitionType {
        ADVANCE,
        CANCEL
    }

    private final OrderRepository orderRepository;
    private final Integer orderId;
    private final TransitionType transitionType;

    private OrderStatus previousStatus;
    private OrderStatus appliedStatus;

    private OrderStatusTransitionCommand(OrderRepository orderRepository, Integer orderId,
                                         TransitionType transitionType) {
        this.orderRepository = orderRepository;
        this.orderId = orderId;
        this.transitionType = transitionType;
    }

    public static OrderStatusTransitionCommand advance(OrderRepository orderRepository, Integer orderId) {
        return new OrderStatusTransitionCommand(orderRepository, orderId, TransitionType.ADVANCE);
    }

    public static OrderStatusTransitionCommand cancel(OrderRepository orderRepository, Integer orderId) {
        return new OrderStatusTransitionCommand(orderRepository, orderId, TransitionType.CANCEL);
    }

    @Override
    public void execute() {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay don hang #" + orderId));

        previousStatus = order.getStatus();
        OrderContext context = new OrderContext(order);

        switch (transitionType) {
            case ADVANCE -> context.advance();
            case CANCEL -> context.cancel();
        }

        appliedStatus = order.getStatus();
        orderRepository.save(order);
    }

    @Override
    public void undo() {
        if (previousStatus == null) {
            return;
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay don hang #" + orderId));
        order.setStatus(previousStatus);
        orderRepository.save(order);
    }

    @Override
    public String getDescription() {
        String from = previousStatus != null ? previousStatus.name() : "UNKNOWN";
        String to = appliedStatus != null ? appliedStatus.name() : transitionType.name();
        return "Cap nhat trang thai don #" + orderId + ": " + from + " -> " + to;
    }

    public OrderStatus getPreviousStatus() {
        return previousStatus;
    }

    public OrderStatus getAppliedStatus() {
        return appliedStatus;
    }
}
