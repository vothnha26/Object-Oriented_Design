package com.alotra.service.proxy;

import com.alotra.entity.Customer;
import com.alotra.entity.Review;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.enums.PaymentStatus;
import com.alotra.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Primary
public class ReviewOperationsProxy implements ReviewOperations {
    private static final Logger log = LoggerFactory.getLogger(ReviewOperationsProxy.class);

    private final ReviewOperations real;
    private final ReviewRepository reviewRepository;

    public ReviewOperationsProxy(@Qualifier("reviewOperationsReal") ReviewOperations real,
                                 ReviewRepository reviewRepository) {
        this.real = real;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Map<Integer, Review> findByCustomerAndLineIds(Integer customerId, List<Integer> lineIds) {
        return real.findByCustomerAndLineIds(customerId, lineIds);
    }

    @Override
    public Map<Integer, Review> findExistingByCustomerAndLines(Integer customerId, List<Integer> lineIds) {
        return real.findExistingByCustomerAndLines(customerId, lineIds);
    }

    @Override
    public boolean canEdit(Review r) {
        return real.canEdit(r);
    }

    @Override
    public boolean isOrderEligibleForReview(OrderStatus orderStatus, PaymentStatus paymentStatus) {
        return real.isOrderEligibleForReview(orderStatus, paymentStatus);
    }

    @Override
    public void submitReview(Customer customer, Integer orderLineId, int stars, String comment) {
        real.submitReview(customer, orderLineId, stars, comment);
    }

    @Override
    public Review updateIfAllowed(Customer customer, Integer reviewId, int stars, String comment) {
        validateOwnership(customer, reviewId, "update");
        return real.updateIfAllowed(customer, reviewId, stars, comment);
    }

    @Override
    public void deleteIfAllowed(Customer customer, Integer reviewId) {
        validateOwnership(customer, reviewId, "delete");
        real.deleteIfAllowed(customer, reviewId);
    }

    @Override
    public ReviewRepository.ProductRatingStats statsForProduct(Integer productId) {
        return real.statsForProduct(productId);
    }

    @Override
    public List<Review> listByProduct(Integer productId, Integer limit) {
        return real.listByProduct(productId, limit);
    }

    private void validateOwnership(Customer customer, Integer reviewId, String action) {
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        Integer ownerId = review.getCustomer() == null ? null : review.getCustomer().getId();
        if (!Objects.equals(ownerId, customer.getId())) {
            log.warn("SECURITY: customer {} attempted {} review {} owned by {}",
                    customer.getId(), action, reviewId, ownerId);
            throw new SecurityException("Không thể thao tác đánh giá của người khác");
        }
    }
}