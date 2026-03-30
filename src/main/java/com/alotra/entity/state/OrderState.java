package com.alotra.entity.state;

import com.alotra.entity.enums.OrderStatus;

/**
 * State Pattern Interface cho trạng thái đơn hàng.
 * Mỗi trạng thái (PENDING, PREPARING, DELIVERING, DELIVERED, CANCELLED)
 * được implement bởi một class riêng biệt.
 */
public interface OrderState {

    /**
     * Chuyển đơn hàng sang trạng thái tiếp theo.
     * @param context OrderContext chứa Order hiện tại
     * @throws IllegalStateException nếu không thể advance từ trạng thái này
     */
    void advance(OrderContext context);

    /**
     * Hủy đơn hàng.
     * @param context OrderContext chứa Order hiện tại
     * @throws IllegalStateException nếu không thể hủy từ trạng thái này
     */
    void cancel(OrderContext context);

    /**
     * Kiểm tra xem đơn hàng có thể hủy từ trạng thái này không.
     */
    boolean canCancel();

    /**
     * Kiểm tra xem đơn hàng có thể đánh giá từ trạng thái này không.
     */
    boolean canReview();

    /**
     * Trả về OrderStatus enum tương ứng với state này.
     */
    OrderStatus getStatus();
}
