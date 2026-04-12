package com.alotra.service.order;

import com.alotra.entity.Order;
import com.alotra.entity.OrderItem;
import com.alotra.entity.Promotion;
import com.alotra.service.pricing.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PriceServiceImpl implements PriceService {

    private final com.alotra.repository.PromotionRepository promotionRepository;
    private final List<PromotionApplicator> promotionApplicators;

    public PriceServiceImpl(com.alotra.repository.PromotionRepository promotionRepository,
                            List<PromotionApplicator> promotionApplicators) {
        this.promotionRepository = promotionRepository;
        this.promotionApplicators = promotionApplicators;
    }

    @Override
    public void calculateTotal(Order order, String promotionCode) {
        BigDecimal subTotal = BigDecimal.ZERO;
        
        for (OrderItem item : order.getItems()) {
            calculateItemTotal(item);
            subTotal = subTotal.add(item.getLineTotalAmount());
        }
        
        order.setSubTotal(subTotal);
        
        // --- Xử lý Khuyến mãi linh hoạt (OCP) ---
        PriceComponent finalPriceChain = new BasePrice(subTotal);
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (promotionCode != null && !promotionCode.isBlank()) {
            var promoOpt = promotionRepository.findByCode(promotionCode);
            if (promoOpt.isPresent() && promoOpt.get().isActive()) {
                Promotion promo = promoOpt.get();
                
                if (subTotal.compareTo(promo.getMinOrderAmount()) >= 0) {
                    order.setPromotion(promo);
                    
                    // TÌM APPLICATOR PHÙ HỢP TRONG DANH SÁCH (KHÔNG DÙNG SWITCH/IF)
                    final PriceComponent currentChain = finalPriceChain;
                    finalPriceChain = promotionApplicators.stream()
                            .filter(applicator -> applicator.supports(promo.getType()))
                            .findFirst()
                            .map(applicator -> applicator.apply(currentChain, promo))
                            .orElse(currentChain);
                    
                    discountAmount = subTotal.subtract(finalPriceChain.calculate());
                }
            }
        }
        
        order.setDiscountAmount(discountAmount);
        BigDecimal shipping = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        
        BigDecimal total = finalPriceChain.calculate().add(shipping);
        order.setTotalAmount(total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total);
        
        if (order.getPayment() != null) {
            order.getPayment().setAmount(order.getTotalAmount());
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
