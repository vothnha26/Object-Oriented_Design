package com.alotra.service.order;

import com.alotra.entity.Order;
import com.alotra.entity.OrderItem;
import com.alotra.entity.OrderedTopping;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class PriceServiceImpl implements PriceService {
    @Override
    public void calculateTotal(Order order, String promotionCode) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderItem item : order.getItems()) {
            // Sử dụng logic getLineTotal() có sẵn của OrderItem để đảm bảo đồng bộ
            BigDecimal lineTotal = item.getLineTotal();
            totalAmount = totalAmount.add(lineTotal);
        }
        
        // Cập nhật Payment Amount
        if (order.getPayment() != null) {
            // Áp dụng khuyến mãi (Member 2 sẽ mở rộng sau)
            if (promotionCode != null && !promotionCode.isEmpty()) {
                totalAmount = totalAmount.multiply(new BigDecimal("0.9"));
            }
            order.getPayment().setAmount(totalAmount);
        }
    }
}
