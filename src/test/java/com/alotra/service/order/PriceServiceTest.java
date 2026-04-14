package com.alotra.service.order;

import com.alotra.entity.Order;
import com.alotra.entity.OrderItem;
import com.alotra.entity.Promotion;
import com.alotra.entity.enums.PromotionType;
import com.alotra.repository.PromotionRepository;
import com.alotra.service.pricing.OrderPriceProcessor;
import com.alotra.service.pricing.PromotionApplicator;
import com.alotra.service.pricing.strategy.PercentagePromotionApplicator;
import com.alotra.service.pricing.strategy.ValuePromotionApplicator;
import com.alotra.service.pricing.processor.ItemSubtotalProcessor;
import com.alotra.service.pricing.processor.PromotionProcessor;
import com.alotra.service.pricing.processor.ShippingProcessor;
import com.alotra.service.pricing.processor.RoundingProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class PriceServiceTest {

    private PriceServiceImpl priceService;

    @Mock
    private PromotionRepository promotionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Cấu hình Pipeline cho Unit Test
        List<OrderPriceProcessor> processors = new ArrayList<>();
        processors.add(new ItemSubtotalProcessor());

        // PromotionProcessor cần Applicators
        List<PromotionApplicator> applicators = List.of(
                new PercentagePromotionApplicator(),
                new ValuePromotionApplicator());
        processors.add(new PromotionProcessor(promotionRepository, applicators));

        processors.add(new ShippingProcessor());
        processors.add(new RoundingProcessor());

        priceService = new PriceServiceImpl(processors);
    }

    @Test
    void testCalculateTotal_FullPipeline() {
        // 1. Arrange
        Order order = new Order();
        OrderItem item = new OrderItem();
        item.setUnitPrice(new BigDecimal("100000"));
        item.setQuantity(1);
        order.setItems(List.of(item));
        order.setShippingFee(new BigDecimal("15000")); // 15k ship

        Promotion promo = new Promotion();
        promo.setCode("GIAM10");
        promo.setType(PromotionType.PERCENTAGE);
        promo.setDiscountRate(10); // Giảm 10%
        promo.setMinOrderAmount(new BigDecimal("50000"));
        promo.setStatus(com.alotra.entity.enums.PromotionStatus.ACTIVE);

        when(promotionRepository.findByCode("GIAM10")).thenReturn(Optional.of(promo));

        // 2. Act
        priceService.calculateTotal(order, "GIAM10");

        // 3. Assert
        // Subtotal = 100k
        // Discount = 10k
        // Shipping = 15k
        // Total = (100 - 10) + 15 = 105k
        assertEquals(new BigDecimal("100000"), order.getSubTotal());
        assertEquals(new BigDecimal("10000.0000"), order.getDiscountAmount());
        assertEquals(new BigDecimal("105000"), order.getTotalAmount());
    }
}
