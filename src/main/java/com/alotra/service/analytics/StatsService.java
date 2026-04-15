package com.alotra.service.analytics;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {
    private final JdbcTemplate jdbc;

    public StatsService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public DashboardStats loadDashboardStats() {
        DashboardStats s = new DashboardStats();
        // Revenue: only count orders with PAID status
        s.totalRevenue = nnBig(queryBigDecimal("SELECT SUM(p.amount) FROM orders o JOIN payments p ON p.order_id = o.id WHERE p.status='PAID'"));
        s.totalOrders = nnLong(queryLong("SELECT COUNT(*) FROM orders"));
        s.totalCustomers = nnLong(queryLong("SELECT COUNT(*) FROM customers"));
        s.totalProducts = nnLong(queryLong("SELECT COUNT(*) FROM products WHERE deleted_at IS NULL"));
        s.totalReviews = nnLong(queryLong("SELECT COUNT(*) FROM reviews"));
        s.avgStars = nnDouble(queryDouble("SELECT AVG(stars) FROM reviews"));
        s.revenueDaily = revenueDaily(14);
        s.orderStatus = orderStatusDistribution();
        s.topProducts = topProducts(8);
        s.categorySales = categorySales();
        s.topCustomers = topCustomers(8);
        return s;
    }

    private BigDecimal queryBigDecimal(String sql){ 
        try {
            return jdbc.queryForObject(sql, BigDecimal.class); 
        } catch (Exception e) { return BigDecimal.ZERO; }
    }
    private Long queryLong(String sql){ 
        try {
            return jdbc.queryForObject(sql, Long.class); 
        } catch (Exception e) { return 0L; }
    }
    private Double queryDouble(String sql){ 
        try {
            return jdbc.queryForObject(sql, Double.class); 
        } catch (Exception e) { return 0d; }
    }
    private BigDecimal nnBig(BigDecimal v){ return v==null? BigDecimal.ZERO: v; }
    private long nnLong(Long v){ return v==null?0L:v; }
    private double nnDouble(Double v){ return v==null?0d:v; }

    private List<Map<String,Object>> revenueDaily(int days){
        String sql = "SELECT DATE(o.created_at) d, SUM(p.amount) total FROM orders o " +
                "JOIN payments p ON p.order_id = o.id " +
                "WHERE o.created_at >= CURDATE() - INTERVAL ? DAY " +
                "AND p.status='PAID' " +
                "GROUP BY DATE(o.created_at) ORDER BY d";
        return jdbc.query(sql, ps -> ps.setInt(1, days-1), (rs,i)-> Map.of(
                "date", rs.getDate("d").toLocalDate().toString(),
                "total", rs.getBigDecimal("total") == null ? BigDecimal.ZERO : rs.getBigDecimal("total")
        ));
    }

    private List<Map<String,Object>> orderStatusDistribution(){
        String sql = "SELECT status, COUNT(*) cnt FROM orders GROUP BY status";
        return jdbc.query(sql, (rs,i)-> Map.of(
                "status", rs.getString("status"),
                "count", rs.getLong("cnt")
        ));
    }

    private List<Map<String,Object>> topProducts(int limit){
        String sql = "SELECT sp.id id, sp.name name, SUM(ct.quantity) qty, SUM(ct.quantity * ct.unit_price) amount " +
                "FROM order_items ct JOIN orders o ON o.id = ct.order_id " +
                "JOIN payments p ON p.order_id = o.id " +
                "JOIN product_variants bt ON bt.id = ct.variant_id " +
                "JOIN products sp ON sp.id = bt.product_id " +
                "WHERE p.status='PAID' " +
                "GROUP BY sp.id, sp.name ORDER BY qty DESC " +
                "LIMIT " + limit;
        return jdbc.query(sql, (rs,i)-> Map.of(
                "id", rs.getInt("id"),
                "name", rs.getString("name"),
                "qty", rs.getLong("qty"),
                "amount", rs.getBigDecimal("amount") == null ? BigDecimal.ZERO : rs.getBigDecimal("amount")
        ));
    }

    private List<Map<String,Object>> categorySales(){
        String sql = "SELECT dm.name name, SUM(ct.quantity) qty, SUM(ct.quantity * ct.unit_price) amount " +
                "FROM order_items ct JOIN orders o ON o.id = ct.order_id " +
                "JOIN payments p ON p.order_id = o.id " +
                "JOIN product_variants bt ON bt.id = ct.variant_id " +
                "JOIN products sp ON sp.id = bt.product_id " +
                "LEFT JOIN categories dm ON dm.id = sp.category_id " +
                "WHERE p.status='PAID' " +
                "GROUP BY dm.name ORDER BY amount DESC";
        return jdbc.query(sql, (rs,i)-> Map.of(
                "name", rs.getString("name") == null ? "Không xác định" : rs.getString("name"),
                "qty", rs.getLong("qty"),
                "amount", rs.getBigDecimal("amount") == null ? BigDecimal.ZERO : rs.getBigDecimal("amount")
        ));
    }

    private List<Map<String,Object>> topCustomers(int limit){
        // fullName is directly in customers table because User is @MappedSuperclass
        String sql = "SELECT kh.id id, kh.full_name name, COUNT(o.id) orders, SUM(p.amount) spend " +
                "FROM orders o JOIN customers kh ON kh.id = o.customer_id " +
                "JOIN payments p ON p.order_id = o.id " +
                "WHERE p.status='PAID' " +
                "GROUP BY kh.id, kh.full_name ORDER BY spend DESC " +
                "LIMIT " + limit;
        return jdbc.query(sql, (rs,i)-> Map.of(
                "id", rs.getInt("id"),
                "name", rs.getString("name"),
                "orders", rs.getLong("orders"),
                "spend", rs.getBigDecimal("spend") == null ? BigDecimal.ZERO : rs.getBigDecimal("spend")
        ));
    }

    public static class DashboardStats {
        public BigDecimal totalRevenue;
        public long totalOrders;
        public long totalCustomers;
        public long totalProducts;
        public long totalReviews;
        public double avgStars;
        public List<Map<String,Object>> revenueDaily;
        public List<Map<String,Object>> orderStatus;
        public List<Map<String,Object>> topProducts;
        public List<Map<String,Object>> categorySales;
        public List<Map<String,Object>> topCustomers;
    }
}
