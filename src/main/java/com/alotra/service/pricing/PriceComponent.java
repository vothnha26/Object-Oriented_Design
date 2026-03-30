package com.alotra.service.pricing;

import java.math.BigDecimal;

/**
 * Base component for line price calculation.
 * Decorator pattern for composing price components.
 */
public interface PriceComponent {
    /**
     * Calculate price for this component
     */
    BigDecimal calculate();
}
