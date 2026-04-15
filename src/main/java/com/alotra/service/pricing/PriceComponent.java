package com.alotra.service.pricing;

import java.math.BigDecimal;

/**
 * Interface cơ sở cho Decorator Pattern trong tính giá.
 */
public interface PriceComponent {
    BigDecimal calculate();
    String getDescription();
}
