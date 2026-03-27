package com.alotra.service;

import com.alotra.entity.OrderItem;
import com.alotra.entity.Review;
import com.alotra.entity.Order;
import com.alotra.entity.Customer;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.enums.PaymentStatus;
import com.alotra.repository.OrderItemRepository;
import com.alotra.repository.ReviewRepository;
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
    private final OrderItemRepository orderLineRepo;

    public ReviewService(ReviewRepository reviewRepo, OrderItemRepository orderLineRepo) {
        this.reviewRepo = reviewRepo;
        this.orderLineRepo = orderLineRepo;
    }

    @Override
    public Map<Integer, Review> findByCustomerAndLineIds(Integer customerId, List<Integer> lineIds) {
        if (customerId == null || lineIds == null || lineIds.isEmpty()) return Map.of();
        return reviewRepo.findByCustomer_IdAndOrderLine_IdIn(customerId, lineIds).stream()
                .collect(Collectors.toMap(d -> d.getOrderLine().getId(), d -> d));
    }

    @Override
    public Map<Integer, Review> findExistingByCustomerAndLines(Integer customerId, List<Integer> lineIds) {
        return findByCustomerAndLineIds(customerId, lineIds);
    }

    @Override
    public boolean canEdit(Review r) {
        if (r == null || r.getCreatedAt() == null) return false;
        return Duration.between(r.getCreatedAt(), LocalDateTime.now()).compareTo(EDIT_WINDOW) <= 0;
    }

    @Override
    public boolean isOrderEligibleForReview(OrderStatus orderStatus, PaymentStatus paymentStatus) {
        return orderStatus == OrderStatus.DELIVERED && paymentStatus == PaymentStatus.PAID;
    }

    @Transactional
    @Override
    public void submitReview(Customer customer, Integer orderLineId, int stars, String comment) {
        if (stars < 1 || stars > 5) throw new IllegalArgumentException("Số sao phải từ 1 đến 5");
        OrderItem line = orderLineRepo.findById(orderLineId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dòng đơn hàng"));
        Order order = line.getOrder();
        if (order == null || order.getCustomer() == null || !Objects.equals(order.getCustomer().getId(), customer.getId())) {
            throw new SecurityException("Bạn không có quyền đánh giá dòng đơn này");
        }
        if (order.getPayment() == null || !isOrderEligibleForReview(order.getStatus(), order.getPayment().getStatus())) {
            throw new IllegalStateException("Chỉ đánh giá được khi đơn đã giao và đã thanh toán");
        }
        
        reviewRepo.findByCustomer_IdAndOrderLine_Id(customer.getId(), orderLineId).ifPresent(r -> {
            throw new IllegalStateException("Bạn đã đánh giá dòng này rồi");
        });
        
        Review rv = new Review();
        rv.setCustomer(customer);
        rv.setOrderLine(line);
        rv.setStars(stars);
        rv.setComment(comment);
        rv.setCreatedAt(LocalDateTime.now());
        reviewRepo.save(rv);
    }
    
    @Transactional
    @Override
    public Review updateIfAllowed(Customer customer, Integer reviewId, int stars, String comment) {
        Review r = reviewRepo.findById(reviewId).orElseThrow();
        if (!canEdit(r)) throw new IllegalStateException("Hết thời gian cho phép chỉnh sửa");
        if (stars < 1 || stars > 5) throw new IllegalArgumentException("Số sao phải từ 1..5");
        r.setStars(stars);
        r.setComment((comment != null && !comment.isBlank()) ? comment.trim() : null);
        return reviewRepo.save(r);
    }

    @Transactional
    @Override
    public void deleteIfAllowed(Customer customer, Integer reviewId) {
        Review r = reviewRepo.findById(reviewId).orElseThrow();
        if (!canEdit(r)) throw new IllegalStateException("Hết thời gian cho phép xóa");
        reviewRepo.delete(r);
    }

    @Override
    public ReviewRepository.ProductRatingStats statsForProduct(Integer productId) {
        return reviewRepo.findStatsByProductId(productId);
    }

    @Override
    public List<Review> listByProduct(Integer productId, Integer limit) {
        List<Review> list = reviewRepo.findByProductIdOrderByCreatedAtDesc(productId);
        if (limit != null && limit > 0 && list.size() > limit) return list.subList(0, limit);
        return list;
    }
}