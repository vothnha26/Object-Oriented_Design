package com.alotra.service.query;

import com.alotra.dto.OrderDto;
import com.alotra.entity.Order;
import com.alotra.repository.OrderRepository;

public class CustomerOrderHistoryQuery extends AbstractOrderQuery {
    private final Integer customerId;

    public CustomerOrderHistoryQuery(OrderRepository repository, Integer customerId) {
        super(repository);
        this.customerId = customerId;
    }

    @Override
    protected java.util.List<Order> fetchOrders() {
        return repository.findByCustomerId(customerId);
    }

    @Override
    protected OrderFilterStrategy getFilter() {
        return new CustomerOrderFilter(customerId);
    }

    @Override
    protected OrderDto toDto(Order o) {
        OrderDto dto = new OrderDto();
        dto.setId(o.getId());
        dto.setCreatedAt(o.getCreatedAt());
        
        dto.setStatus(o.getStatus().getCode());
        dto.setStatusDisplay(o.getStatus().getDisplayName());
        
        dto.setTotal(o.calculateTotal());
        if (o.getCustomer() != null) {
            dto.setCustomerName(o.getCustomer().getFullName());
            dto.setCustomerPhone(o.getCustomer().getPhone());
        }

        if (o.getPayment() != null) {
            dto.setPaymentStatus(o.getPayment().getStatus().getCode());
            dto.setPaymentStatusDisplay(o.getPayment().getStatus().getDisplayName());
            dto.setPaymentMethod(o.getPayment().getMethod().getCode());
            dto.setPaymentMethodDisplay(o.getPayment().getMethod().getDisplayName());
        } else {
            dto.setPaymentStatus(com.alotra.entity.enums.PaymentStatus.UNPAID.getCode());
            dto.setPaymentStatusDisplay(com.alotra.entity.enums.PaymentStatus.UNPAID.getDisplayName());
            dto.setPaymentMethod(com.alotra.entity.enums.PaymentMethod.CASH.getCode());
            dto.setPaymentMethodDisplay(com.alotra.entity.enums.PaymentMethod.CASH.getDisplayName());
        }

        return dto;
    }
}
