package com.alotra.entity.state;

import com.alotra.entity.enums.OrderStatus;

/**
 * Trạng thái "Đã hủy" (CANCELLED) — trạng thái cuối cùng thất bại.
 * - KHÔNG thể advance
 * - KHÔNG thể cancel (đã hủy rồi)
 */
public class CancelledState implements OrderState {

    @Override
    public void advance(OrderContext context) {
        throw new IllegalStateException("Đơn hàng đã hủy, không thể chuyển trạng thái");
    }

    @Override
    public void cancel(OrderContext context) {
        throw new IllegalStateException("Đơn hàng đã được hủy trước đó");
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
        return OrderStatus.CANCELLED;
    }
}
