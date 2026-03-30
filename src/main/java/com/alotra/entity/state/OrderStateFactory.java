package com.alotra.entity.state;

import com.alotra.entity.enums.OrderStatus;

/**
 * Factory tạo OrderState từ OrderStatus enum.
 * Đây là điểm duy nhất cần switch — khi thêm trạng thái mới,
 * chỉ cần thêm 1 case ở đây + 1 class mới.
 */
public class OrderStateFactory {

    public static OrderState fromStatus(OrderStatus status) {
        if (status == null) {
            return new PendingState();
        }
        return switch (status) {
            case PENDING -> new PendingState();
            case PREPARING -> new PreparingState();
            case DELIVERING -> new DeliveringState();
            case DELIVERED -> new DeliveredState();
            case CANCELLED -> new CancelledState();
        };
    }
}
