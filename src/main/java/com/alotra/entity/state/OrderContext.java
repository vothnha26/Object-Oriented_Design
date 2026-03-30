package com.alotra.entity.state;

import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;

/**
 * Context trong State Pattern — giữ reference tới Order entity và state hiện tại.
 * Khi state thay đổi, tự động đồng bộ OrderStatus vào Order entity.
 *
 * Cách sử dụng:
 * <pre>
 *     OrderContext ctx = new OrderContext(order);
 *     ctx.advance();   // chuyển trạng thái tiếp
 *     ctx.cancel();    // hủy đơn
 *     // order.getStatus() đã được cập nhật tự động
 * </pre>
 */
public class OrderContext {

    private OrderState state;
    private final Order order;

    public OrderContext(Order order) {
        this.order = order;
        this.state = OrderStateFactory.fromStatus(order.getStatus());
    }

    /**
     * Chuyển trạng thái tiếp theo.
     * @throws IllegalStateException nếu không thể advance từ trạng thái hiện tại
     */
    public void advance() {
        state.advance(this);
    }

    /**
     * Hủy đơn hàng.
     * @throws IllegalStateException nếu không thể hủy từ trạng thái hiện tại
     */
    public void cancel() {
        state.cancel(this);
    }

    public boolean canCancel() {
        return state.canCancel();
    }

    public boolean canReview() {
        return state.canReview();
    }

    public OrderStatus getStatus() {
        return state.getStatus();
    }

    public OrderState getState() {
        return state;
    }

    /**
     * Được gọi bởi các concrete State khi chuyển trạng thái.
     * Tự động đồng bộ OrderStatus vào Order entity.
     */
    public void setState(OrderState newState) {
        this.state = newState;
        this.order.setStatus(newState.getStatus());
    }

    public Order getOrder() {
        return order;
    }
}
