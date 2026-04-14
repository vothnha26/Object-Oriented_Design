package com.alotra.service.query;

import com.alotra.entity.Order;
import java.time.LocalDateTime;

public class DateRangeFilter implements OrderFilterStrategy {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public DateRangeFilter(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean matches(Order order) {
        LocalDateTime created = order.getCreatedAt();
        if (created == null) return false;
        
        boolean afterStart = (start == null) || !created.isBefore(start);
        boolean beforeEnd = (end == null) || !created.isAfter(end);
        
        return afterStart && beforeEnd;
    }
}
