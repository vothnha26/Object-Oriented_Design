package com.alotra.entity.state;

import com.alotra.entity.enums.OrderStatus;

/**
 * Trạng thái "Chờ xử lý" (PENDING).
 * - Có thể advance → PreparingState
 * - Có thể cancel → CancelledState
 */
public class PendingState implements OrderState {

    @Override
    public void advance(OrderContext context) {
        context.setState(new PreparingState());
    }

    @Override
    public void cancel(OrderContext context) {
        context.setState(new CancelledState());
    }

    @Override
    public boolean canCancel() {
        return true;
    }

    @Override
    public boolean canReview() {
        return false;
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PENDING;
    }
}
