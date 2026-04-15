package com.alotra.service.query;

import com.alotra.entity.Order;

public class AssignedToEmployeeFilter implements OrderFilterStrategy {
    private final Integer employeeId;

    public AssignedToEmployeeFilter(Integer employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public boolean matches(Order order) {
        if (employeeId == null) return true;
        return order.getApprovedBy() != null && java.util.Objects.equals(order.getApprovedBy().getId(), employeeId);
    }
}
