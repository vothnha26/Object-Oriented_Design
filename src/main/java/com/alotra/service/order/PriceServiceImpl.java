package com.alotra.service.order;

import com.alotra.entity.Order;
import com.alotra.entity.OrderItem;
import com.alotra.service.pricing.PriceComponent;
import com.alotra.service.pricing.OrderPriceProcessor;
import com.alotra.service.pricing.component.BasePrice;
import com.alotra.service.pricing.decorator.ToppingDecorator;
import com.alotra.service.pricing.decorator.QuantityDecorator;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PriceServiceImpl implements PriceService {

    private final List<OrderPriceProcessor> processors;

    public PriceServiceImpl(List<OrderPriceProcessor> processors) {
        // Sắp xếp các Processor theo thứ tự getOrder() để đảm bảo đúng quy trình
        this.processors = processors.stream()
                .sorted(Comparator.comparingInt(OrderPriceProcessor::getOrder))
                .collect(Collectors.toList());
    }

    @Override
    public void calculateTotal(Order order, String promotionCode) {
        // 1. Tính toán giá trị từng Item trước (Topping, Quantity)
        for (OrderItem item : order.getItems()) {
            calculateItemTotal(item);
        }

        // 2. Chạy qua Pipeline (Chain of Responsibility) để tính toán Order Total
        PriceComponent chain = null; 
        for (OrderPriceProcessor processor : processors) {
            chain = processor.process(chain, order, promotionCode);
        }

        // 3. Gán kết quả cuối cùng vào đơn hàng
        if (chain != null) {
            BigDecimal total = chain.calculate();
            order.setTotalAmount(total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total);
            
            // Cập nhật số tiền thanh toán nếu có thông tin Payment
            if (order.getPayment() != null) {
                order.getPayment().setAmount(order.getTotalAmount());
            }
        }
    }

    @Override
    public void calculateItemTotal(OrderItem item) {
        PriceComponent price = new BasePrice(item.getUnitPrice());
        
        if (item.getToppings() != null && !item.getToppings().isEmpty()) {
            price = new ToppingDecorator(price, item.getToppings());
        }
        
        price = new QuantityDecorator(price, item.getQuantity());
        
        item.setLineTotalAmount(price.calculate());
    }
}
