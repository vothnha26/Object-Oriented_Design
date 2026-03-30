package com.alotra.service;

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
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Orders dh JOIN Payment p ON p.OrderId = dh.MaDH WHERE dh.MaKH = ? AND p.Status <> 'PAID'",
                Integer.class, customerId);
        return n == null ? 0 : n;
    }

    private int countReviewableItems(Integer customerId) {
        // Count order lines from delivered + paid orders without a review by this customer
        String sql = "SELECT COUNT(*) FROM OrderItem ct " +
                "JOIN Orders dh ON dh.MaDH = ct.MaDH " +
                "JOIN Payment p ON p.OrderId = dh.MaDH " +
                "WHERE dh.MaKH = ? AND dh.TrangThaiDonHang = 'DELIVERED' AND p.Status = 'PAID' " +
                "AND NOT EXISTS (SELECT 1 FROM Review dg WHERE dg.MaCT = ct.MaCT AND dg.MaKH = dh.MaKH)";
        Integer n = jdbc.queryForObject(sql, Integer.class, customerId);
        return n == null ? 0 : n;
    }

    private List<Map<String, Object>> listRecentOrders(Integer customerId, int limit) {
        String sql = "SELECT MaDH, TrangThaiDonHang, NgayLap FROM Orders WHERE MaKH = ? ORDER BY MaDH DESC LIMIT " + Math.max(1, limit);
        return jdbc.query(sql, ps -> ps.setInt(1, customerId), (rs, i) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", rs.getInt("MaDH"));
            m.put("status", rs.getString("TrangThaiDonHang"));
            java.sql.Timestamp ts = rs.getTimestamp("NgayLap");
            m.put("createdAt", ts != null ? ts.toInstant().atZone(HCM_ZONE).toOffsetDateTime() : null);
            return m;
        });
    }
}