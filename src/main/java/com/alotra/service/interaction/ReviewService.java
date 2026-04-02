package com.alotra.service.interaction;

import com.alotra.entity.*;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.enums.PaymentStatus;
import com.alotra.repository.*;
import com.alotra.service.proxy.ReviewOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service("reviewOperationsReal")
public class ReviewService implements ReviewOperations {
    public static final Duration EDIT_WINDOW = Duration.ofMinutes(15);

    private final ReviewRepository reviewRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    public ReviewService(ReviewRepository reviewRepo, ProductRepository productRepo, OrderRepository orderRepo) {
        this.reviewRepo = reviewRepo;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
    }

    public Map<Integer, Review> findByCustomerAndProductIds(Integer customerId, List<Integer> productIds) {
        if (customerId == null || productIds == null || productIds.isEmpty()) return Map.of();
        return reviewRepo.findByCustomerIdAndProductIdIn(customerId, productIds).stream()
                .collect(Collectors.toMap(r -> r.getProduct().getId(), r -> r));
    }

    public boolean canEdit(Review r) {
        if (r == null || r.getCreatedAt() == null) return false;
        return Duration.between(r.getCreatedAt(), LocalDateTime.now()).compareTo(EDIT_WINDOW) <= 0;
    }

    public boolean isOrderEligibleForReview(OrderStatus orderStatus, PaymentStatus paymentStatus) {
        return orderStatus == OrderStatus.DELIVERED && paymentStatus == PaymentStatus.PAID;
    }

    @Transactional
    public void submitReview(Customer customer, Integer productId, Integer orderId, int stars, String comment) {
        if (stars < 1 || stars > 5) throw new IllegalArgumentException("Số sao phải từ 1 đến 5");
        
        Product product = productRepo.findById(productId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        if (!Objects.equals(order.getCustomer().getId(), customer.getId())) {
            throw new SecurityException("Bạn không có quyền đánh giá đơn hàng này");
        }
        
        if (order.getPayment() == null || !isOrderEligibleForReview(order.getStatus(), order.getPayment().getStatus())) {
            throw new IllegalStateException("Chỉ đánh giá được khi đơn đã giao và đã thanh toán");
        }
        
        reviewRepo.findByCustomerIdAndProductIdAndOrderId(customer.getId(), productId, orderId).ifPresent(r -> {
            throw new IllegalStateException("Bạn đã đánh giá sản phẩm này trong đơn hàng này rồi");
        });
        
        Review rv = new Review();
        rv.setCustomer(customer);
        rv.setProduct(product);
        rv.setOrder(order);
        rv.setStars(stars);
        rv.setComment(comment);
        rv.setCreatedAt(LocalDateTime.now());
        reviewRepo.save(rv);
    }
    
    @Transactional
    public Review updateIfAllowed(Customer customer, Integer reviewId, int stars, String comment) {
        Review r = reviewRepo.findById(reviewId).orElseThrow();
        if (!Objects.equals(r.getCustomer().getId(), customer.getId())) {
            throw new SecurityException("Không thể sửa đánh giá của người khác");
        }
        if (!canEdit(r)) throw new IllegalStateException("Hết thời gian cho phép chỉnh sửa");
        if (stars < 1 || stars > 5) throw new IllegalArgumentException("Số sao phải từ 1..5");
        r.setStars(stars);
        r.setComment((comment != null && !comment.isBlank()) ? comment.trim() : null);
        return reviewRepo.save(r);
    }

    @Transactional
    public void deleteIfAllowed(Customer customer, Integer reviewId) {
        Review r = reviewRepo.findById(reviewId).orElseThrow();
        if (!Objects.equals(r.getCustomer().getId(), customer.getId())) {
            throw new SecurityException("Không thể xóa đánh giá của người khác");
        }
        if (!canEdit(r)) throw new IllegalStateException("Hết thời gian cho phép xóa");
        reviewRepo.delete(r);
    }

    public ReviewRepository.ProductRatingStats statsForProduct(Integer productId) {
        return reviewRepo.findStatsByProductId(productId);
    }

    public List<Review> listByProduct(Integer productId, Integer limit) {
        List<Review> list = reviewRepo.findByProductIdOrderByCreatedAtDesc(productId);
        if (limit != null && limit > 0 && list.size() > limit) return list.subList(0, limit);
        return list;
    }
}
