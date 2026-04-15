package com.alotra.service.proxy;

import com.alotra.entity.Customer;
import com.alotra.entity.Review;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.enums.PaymentStatus;
import com.alotra.repository.ReviewRepository;
import com.alotra.service.interaction.ReviewOperations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Primary
public class ReviewOperationsProxy implements ReviewOperations {

    private final ReviewOperations realService;
    
    public ReviewOperationsProxy(@Qualifier("reviewOperationsReal") ReviewOperations realService) {
        this.realService = realService;
    }

    @Override
    public Map<Integer, Review> findByCustomerAndProductIds(Integer customerId, List<Integer> productIds) {
        return realService.findByCustomerAndProductIds(customerId, productIds);
    }

    @Override
    public boolean canEdit(Review r) {
        return realService.canEdit(r);
    }

    @Override
    public boolean isOrderEligibleForReview(OrderStatus orderStatus, PaymentStatus paymentStatus) {
        return realService.isOrderEligibleForReview(orderStatus, paymentStatus);
    }

    @Override
    public void submitReview(Customer customer, Integer productId, Integer orderId, int stars, String comment) {
        // Proxy logic can be added here (logging, extra security, etc.)
        realService.submitReview(customer, productId, orderId, stars, comment);
    }

    @Override
    public Review updateIfAllowed(Customer customer, Integer reviewId, int stars, String comment) {
        return realService.updateIfAllowed(customer, reviewId, stars, comment);
    }

    @Override
    public void deleteIfAllowed(Customer customer, Integer reviewId) {
        realService.deleteIfAllowed(customer, reviewId);
    }

    @Override
    public ReviewRepository.ProductRatingStats statsForProduct(Integer productId) {
        return realService.statsForProduct(productId);
    }

    @Override
    public List<Review> listByProduct(Integer productId, Integer limit) {
        return realService.listByProduct(productId, limit);
    }
}
