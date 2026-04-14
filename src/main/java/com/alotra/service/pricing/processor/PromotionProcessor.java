package com.alotra.service.pricing.processor;

import com.alotra.entity.Order;
import com.alotra.entity.Promotion;
import com.alotra.repository.PromotionRepository;
import com.alotra.service.pricing.PriceComponent;
import com.alotra.service.pricing.OrderPriceProcessor;
import com.alotra.service.pricing.PromotionApplicator;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

/**
 * Trạm 2: Tìm và áp dụng khuyến mãi nếu mã code hợp lệ.
 */
@Component
public class PromotionProcessor implements OrderPriceProcessor {
    private final PromotionRepository promotionRepository;
    private final List<PromotionApplicator> promotionApplicators;

    public PromotionProcessor(PromotionRepository promotionRepository, 
                                List<PromotionApplicator> promotionApplicators) {
        this.promotionRepository = promotionRepository;
        this.promotionApplicators = promotionApplicators;
    }

    @Override
    public int getOrder() { return 20; }

    @Override
    public PriceComponent process(PriceComponent chain, Order order, String promoCode) {
        if (promoCode == null || promoCode.isBlank()) {
            order.setDiscountAmount(BigDecimal.ZERO);
            return chain;
        }

        var promoOpt = promotionRepository.findByCode(promoCode);
        if (promoOpt.isPresent() && promoOpt.get().isActive()) {
            Promotion promo = promoOpt.get();
            BigDecimal subTotal = order.getSubTotal();

            if (subTotal.compareTo(promo.getMinOrderAmount()) >= 0) {
                order.setPromotion(promo);
                
                // Chọn Strategy phù hợp (OCP)
                PriceComponent discountedChain = promotionApplicators.stream()
                        .filter(applicator -> applicator.supports(promo.getType()))
                        .findFirst()
                        .map(applicator -> applicator.apply(chain, promo))
                        .orElse(chain);
                
                // Tính toán số tiền đã giảm (trước khi làm tròn)
                order.setDiscountAmount(subTotal.subtract(discountedChain.calculate()));
                return discountedChain;
            }
        }
        
        order.setDiscountAmount(BigDecimal.ZERO);
        return chain;
    }
}
