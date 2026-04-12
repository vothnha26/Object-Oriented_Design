package com.alotra.service.infrastructure;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;

@Service
public class NotificationService {
    private static final ZoneId HCM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final JdbcTemplate jdbc;

    public NotificationService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Map<String, Object> getCustomerNotifications(Integer customerId) {
        if (customerId == null) return Map.of("items", List.of(), "count", 0);
        int unpaid = countUnpaidOrders(customerId);
        int needReview = countReviewableItems(customerId);
        List<Map<String, Object>> recentOrders = listRecentOrders(customerId, 5);

        List<Map<String, Object>> items = new ArrayList<>();
        if (unpaid > 0) {
            items.add(Map.of(
                    "type", "unpaid",
                    "text", "Bạn có " + unpaid + " đơn chưa thanh toán",
                    "url", "/account/orders"
            ));
        }
        if (needReview > 0) {
            items.add(Map.of(
                    "type", "review",
                    "text", "Bạn có " + needReview + " sản phẩm cần đánh giá",
                    "url", "/account/orders"
            ));
        }
        for (Map<String, Object> r : recentOrders) {
            Integer id = (Integer) r.get("id");
            String st = String.valueOf(r.get("status"));
            items.add(Map.of(
                    "type", "order",
                    "text", "Đơn #" + id + " · Trạng thái: " + st,
                    "url", "/account/orders/" + id
            ));
        }
        int count = items.size();
        return Map.of("items", items, "count", count);
    }

    private int countUnpaidOrders(Integer customerId) {
        // Kiểm tra đơn hàng chưa thanh toán (status != PAID trong bảng payments)
        String sql = "SELECT COUNT(*) FROM orders o " +
                     "LEFT JOIN payments p ON o.id = p.order_id " +
                     "WHERE o.customer_id = ? AND (p.status IS NULL OR p.status <> 'PAID')";
        Integer n = jdbc.queryForObject(sql, Integer.class, customerId);
        return n == null ? 0 : n;
    }

    private int countReviewableItems(Integer customerId) {
        // Đếm các mục hàng từ đơn đã giao và đã thanh toán nhưng chưa có đánh giá
        String sql = "SELECT COUNT(*) FROM order_items oi " +
                "JOIN orders o ON o.id = oi.order_id " +
                "JOIN payments p ON o.id = p.order_id " +
                "WHERE o.customer_id = ? AND o.status = 'DELIVERED' AND p.status = 'PAID' " +
                "AND NOT EXISTS (SELECT 1 FROM reviews r WHERE r.product_id = (SELECT product_id FROM product_variants WHERE id = oi.variant_id) AND r.customer_id = o.customer_id)";
        Integer n = jdbc.queryForObject(sql, Integer.class, customerId);
        return n == null ? 0 : n;
    }

    private List<Map<String, Object>> listRecentOrders(Integer customerId, int limit) {
        String sql = "SELECT id, status, created_at FROM orders WHERE customer_id = ? ORDER BY id DESC LIMIT ?";
        return jdbc.query(sql, ps -> {
            ps.setInt(1, customerId);
            ps.setInt(2, Math.max(1, limit));
        }, (rs, i) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", rs.getInt("id"));
            m.put("status", rs.getString("status"));
            java.sql.Timestamp ts = rs.getTimestamp("created_at");
            m.put("createdAt", ts != null ? ts.toInstant().atZone(HCM_ZONE).toOffsetDateTime() : null);
            return m;
        });
    }
}
