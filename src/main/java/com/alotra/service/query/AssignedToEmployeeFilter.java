package com.alotra.service.query;

import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.enums.PaymentStatus;

import java.util.List;

public class AssignedToEmployeeFilter implements OrderFilterStrategy {
    private final Integer employeeId;
    private final List<OrderStatus> statuses;

    public AssignedToEmployeeFilter(Integer employeeId, List<OrderStatus> statuses) {
        this.employeeId = employeeId;
        this.statuses = statuses;
    }

    @Override
    public boolean matches(Order order) {
        boolean matchEmp = order.getEmployee() != null && employeeId.equals(order.getEmployee().getId());
        boolean matchStatus = statuses.contains(order.getStatus());
        
        boolean isCompleted = OrderStatus.DELIVERED.equals(order.getStatus()) && 
                              order.getPayment() != null && 
                              PaymentStatus.PAID.equals(order.getPayment().getStatus());
                              
        return matchEmp && matchStatus && !isCompleted;
    }
}
