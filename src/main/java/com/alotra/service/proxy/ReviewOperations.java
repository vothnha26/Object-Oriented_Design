package com.alotra.service.proxy;

import com.alotra.entity.Customer;
import com.alotra.entity.Review;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.enums.PaymentStatus;
import com.alotra.repository.ReviewRepository;

import java.util.List;
import java.util.Map;

public interface ReviewOperations {
    Map<Integer, Review> findByCustomerAndLineIds(Integer customerId, List<Integer> lineIds);

    Map<Integer, Review> findExistingByCustomerAndLines(Integer customerId, List<Integer> lineIds);

    boolean canEdit(Review r);

    boolean isOrderEligibleForReview(OrderStatus orderStatus, PaymentStatus paymentStatus);

    void submitReview(Customer customer, Integer orderLineId, int stars, String comment);

    Review updateIfAllowed(Customer customer, Integer reviewId, int stars, String comment);

    void deleteIfAllowed(Customer customer, Integer reviewId);

    ReviewRepository.ProductRatingStats statsForProduct(Integer productId);

    List<Review> listByProduct(Integer productId, Integer limit);
}