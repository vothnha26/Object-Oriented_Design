package com.alotra.service.command;

import com.alotra.repository.OrderRepository;

public class UpdateOrderStatusCommand implements AdminCommand {
    private final OrderStatusTransitionCommand delegate;
    private final Integer orderId;
    private String newStatus;
    private String previousStatus;

    private UpdateOrderStatusCommand(OrderStatusTransitionCommand delegate, Integer orderId, String newStatus) {
        this.delegate = delegate;
        this.orderId = orderId;
        this.newStatus = newStatus;
    }

    public static UpdateOrderStatusCommand advance(OrderRepository orderRepository, Integer orderId) {
        return new UpdateOrderStatusCommand(OrderStatusTransitionCommand.advance(orderRepository, orderId), orderId, "ADVANCE");
    }

    public static UpdateOrderStatusCommand cancel(OrderRepository orderRepository, Integer orderId) {
        return new UpdateOrderStatusCommand(OrderStatusTransitionCommand.cancel(orderRepository, orderId), orderId, "CANCEL");
    }

    @Override
    public void execute() {
        delegate.execute();
        previousStatus = delegate.getPreviousStatus() != null ? delegate.getPreviousStatus().name() : null;
        newStatus = delegate.getAppliedStatus() != null ? delegate.getAppliedStatus().name() : newStatus;
    }

    @Override
    public void undo() {
        delegate.undo();
    }

    @Override
    public String getDescription() {
        return "Cập nhật đơn hàng #" + orderId + ": " + (previousStatus != null ? previousStatus : "???") + " -> " + newStatus;
    }
}
