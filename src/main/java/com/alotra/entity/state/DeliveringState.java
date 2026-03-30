package com.alotra.entity.state;

import com.alotra.entity.enums.OrderStatus;

/**
 * Trạng thái "Đang giao hàng" (DELIVERING).
 * - Có thể advance → DeliveredState
 * - KHÔNG thể cancel (đơn đang được giao)
 */
public class DeliveringState implements OrderState {

    @Override
    public void advance(OrderContext context) {
        context.setState(new DeliveredState());
    }

    @Override
    public void cancel(OrderContext context) {
        throw new IllegalStateException("Không thể hủy đơn hàng đang giao");
    }

    @Override
    public boolean canCancel() {
        return false;
    }

    @Override
    public boolean canReview() {
        return false;
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.DELIVERING;
    }
}
