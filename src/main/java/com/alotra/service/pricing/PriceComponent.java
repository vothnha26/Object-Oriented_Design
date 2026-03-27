package com.alotra.service.pricing;

import java.math.BigDecimal;

public interface PriceComponent {
    BigDecimal calculate();
    String getDescription();
}
