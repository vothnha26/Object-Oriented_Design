package com.alotra.service.pricing;

import com.alotra.entity.Promotion;
import com.alotra.entity.enums.PromotionType;

/**
 * Interface cho các chiến lược áp dụng Decorator khuyến mãi.
 * Giúp tuân thủ nguyên tắc OCP: thêm loại mới mà không sửa code cũ.
 */
public interface PromotionApplicator {
    boolean supports(PromotionType type);
    PriceComponent apply(PriceComponent base, Promotion promotion);
}
