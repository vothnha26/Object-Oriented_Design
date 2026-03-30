package com.alotra.entity.state;

import com.alotra.entity.enums.OrderStatus;

/**
 * Trạng thái "Đã giao" (DELIVERED) — trạng thái cuối cùng thành công.
 * - KHÔNG thể advance (đã hoàn thành)
 * - KHÔNG thể cancel
 * - CÓ thể review (đánh giá sản phẩm)
 */
public class DeliveredState implements OrderState {

    @Override
    public void advance(OrderContext context) {
        throw new IllegalStateException("Đơn hàng đã giao, không thể chuyển trạng thái tiếp");
    }

    @Override
    public void cancel(OrderContext context) {
        throw new IllegalStateException("Không thể hủy đơn hàng đã giao");
    }

    @Override
    public boolean canCancel() {
        return false;
    }

    @Override
    public boolean canReview() {
        return true;
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.DELIVERED;
    }
}
