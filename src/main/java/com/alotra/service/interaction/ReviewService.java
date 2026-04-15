package com.alotra.service.interaction;

import com.alotra.dto.ReviewDto;
import com.alotra.entity.Customer;
import com.alotra.entity.Review;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.enums.PaymentStatus;
import com.alotra.repository.ReviewRepository;
import com.alotra.repository.CustomerRepository;
import com.alotra.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("reviewOperationsReal")
public class ReviewService implements ReviewOperations {
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<Review> listByProduct(Integer productId, Integer limit) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        if (limit != null && limit > 0 && reviews.size() > limit) {
            return reviews.subList(0, limit);
        }
        return reviews;
    }

    public List<ReviewDto> listByProductAsDto(Integer productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ReviewDto toDto(Review r) {
        ReviewDto dto = new ReviewDto();
        dto.setId(r.getId());
        dto.setStars(r.getStars());
        dto.setComment(r.getComment());
        dto.setAdminReply(r.getAdminReply());
        
        customerRepository.findById(r.getUserId())
                .ifPresent(c -> dto.setCustomerName(c.getFullName()));
        
        return dto;
    }

    @Override
    public ReviewRepository.ProductRatingStats statsForProduct(Integer productId) {
        return reviewRepository.findStatsByProductId(productId);
    }

    @Override
    public Map<Integer, Review> findByCustomerAndProductIds(Integer customerId, List<Integer> productIds) {
        List<Review> reviews = reviewRepository.findByUserId(customerId);
        return reviews.stream()
                .filter(r -> productIds.contains(r.getProductId()))
                .collect(Collectors.toMap(Review::getProductId, r -> r, (r1, r2) -> r1));
    }

    @Override
    public boolean canEdit(Review r) {
        return !r.hasReply();
    }

    @Override
    public boolean isOrderEligibleForReview(OrderStatus orderStatus, PaymentStatus paymentStatus) {
        return orderStatus == OrderStatus.DELIVERED && paymentStatus == PaymentStatus.PAID;
    }

    @Override
    public void submitReview(Customer customer, Integer productId, Integer orderId, int stars, String comment) {
        // Verification logic could go here (check if order belongs to customer and contains product)
        Review rv = new Review();
        rv.setUserId(customer.getId());
        rv.setProductId(productId);
        rv.setStars(stars);
        rv.setComment(comment);
        reviewRepository.save(rv);
    }

    @Override
    public Review updateIfAllowed(Customer customer, Integer reviewId, int stars, String comment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đánh giá"));
        
        if (!Objects.equals(review.getUserId(), customer.getId())) {
            throw new SecurityException("Không có quyền chỉnh sửa đánh giá này");
        }
        
        if (!canEdit(review)) {
            throw new IllegalStateException("Không thể chỉnh sửa đánh giá đã có phản hồi từ quản trị viên");
        }

        review.setStars(stars);
        review.setComment(comment);
        return reviewRepository.save(review);
    }

    @Override
    public void deleteIfAllowed(Customer customer, Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đánh giá"));
        
        if (!Objects.equals(review.getUserId(), customer.getId())) {
            throw new SecurityException("Không có quyền xóa đánh giá này");
        }

        reviewRepository.delete(review);
    }

    public List<Review> getMyReviews(Customer customer) {
        return reviewRepository.findByUserId(customer.getId());
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
}
