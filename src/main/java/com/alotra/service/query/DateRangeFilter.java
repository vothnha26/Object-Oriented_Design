package com.alotra.service.query;

import com.alotra.entity.Order;
import java.time.LocalDateTime;

public class DateRangeFilter implements OrderFilterStrategy {
    private final LocalDateTime from;
    private final LocalDateTime to;

    public DateRangeFilter(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean matches(Order order) {
        if (order.getCreatedAt() == null) return false;
        if (from != null && order.getCreatedAt().isBefore(from)) return false;
        if (to != null && order.getCreatedAt().isAfter(to)) return false;
        return true;
    }
}
