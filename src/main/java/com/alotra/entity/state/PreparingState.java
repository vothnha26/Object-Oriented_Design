package com.alotra.entity.state;

import com.alotra.entity.enums.OrderStatus;

/**
 * Trạng thái "Đang pha chế" (PREPARING).
 * - Có thể advance → DeliveringState
 * - Có thể cancel → CancelledState
 */
public class PreparingState implements OrderState {

    @Override
    public void advance(OrderContext context) {
        context.setState(new DeliveringState());
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
        return OrderStatus.PREPARING;
    }
}
