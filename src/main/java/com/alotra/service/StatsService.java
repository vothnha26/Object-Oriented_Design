package com.alotra.service;

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
        // Revenue: only count orders with PAID status in Payment table.
        s.totalRevenue = nnBig(queryBigDecimal(
            "SELECT SUM(dh.TongThanhToan) FROM Orders dh " +
            "JOIN Payment tt ON tt.OrderId = dh.MaDH " +
            "WHERE tt.Status='PAID'"
        ));
        s.totalOrders = nnLong(queryLong("SELECT COUNT(*) FROM Orders"));
        s.totalCustomers = nnLong(queryLong("SELECT COUNT(*) FROM Customer"));
        s.totalProducts = nnLong(queryLong("SELECT COUNT(*) FROM Product WHERE DeletedAt IS NULL"));
        s.totalReviews = nnLong(queryLong("SELECT COUNT(*) FROM Review"));
        s.avgStars = nnDouble(queryDouble("SELECT AVG(SoSao) FROM Review"));
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
        // MySQL syntax: DATE(NgayLap), CURDATE() - INTERVAL ? DAY
        String sql = "SELECT DATE(dh.NgayLap) d, SUM(dh.TongThanhToan) total " +
            "FROM Orders dh JOIN Payment tt ON tt.OrderId = dh.MaDH " +
            "WHERE dh.NgayLap >= CURDATE() - INTERVAL ? DAY " +
            "AND tt.Status='PAID' " +
            "GROUP BY DATE(dh.NgayLap) ORDER BY d";
        return jdbc.query(sql, ps -> ps.setInt(1, days-1), (rs,i)-> Map.of(
                "date", rs.getDate("d").toLocalDate().toString(),
                "total", rs.getBigDecimal("total") == null ? BigDecimal.ZERO : rs.getBigDecimal("total")
        ));
    }

    private List<Map<String,Object>> orderStatusDistribution(){
        // Column: TrangThaiDonHang
        String sql = "SELECT TrangThaiDonHang status, COUNT(*) cnt FROM Orders GROUP BY TrangThaiDonHang";
        return jdbc.query(sql, (rs,i)-> Map.of(
                "status", rs.getString("status"),
                "count", rs.getLong("cnt")
        ));
    }

    private List<Map<String,Object>> topProducts(int limit){
        // OrderItem: MaDH, MaBT, ThanhTien, SoLuong
        // ProductVariant: Id, ProductId
        // Product: MaSP, TenSP
        String sql = "SELECT sp.MaSP id, sp.TenSP name, SUM(ct.SoLuong) qty, SUM(ct.ThanhTien) amount " +
                "FROM OrderItem ct JOIN Orders dh ON dh.MaDH = ct.MaDH " +
            "JOIN Payment tt ON tt.OrderId = dh.MaDH " +
                "JOIN ProductVariant bt ON bt.Id = ct.MaBT " +
                "JOIN Product sp ON sp.MaSP = bt.ProductId " +
            "WHERE tt.Status='PAID' " +
                "GROUP BY sp.MaSP, sp.TenSP ORDER BY qty DESC " +
                "LIMIT " + limit;
        return jdbc.query(sql, (rs,i)-> Map.of(
                "id", rs.getInt("id"),
                "name", rs.getString("name"),
                "qty", rs.getLong("qty"),
                "amount", rs.getBigDecimal("amount") == null ? BigDecimal.ZERO : rs.getBigDecimal("amount")
        ));
    }

    private List<Map<String,Object>> categorySales(){
        // Category: MaDM, TenDM
        String sql = "SELECT dm.TenDM name, SUM(ct.SoLuong) qty, SUM(ct.ThanhTien) amount " +
                "FROM OrderItem ct JOIN Orders dh ON dh.MaDH = ct.MaDH " +
            "JOIN Payment tt ON tt.OrderId = dh.MaDH " +
                "JOIN ProductVariant bt ON bt.Id = ct.MaBT " +
                "JOIN Product sp ON sp.MaSP = bt.ProductId " +
                "LEFT JOIN Category dm ON dm.MaDM = sp.MaDM " +
            "WHERE tt.Status='PAID' " +
                "GROUP BY dm.TenDM ORDER BY amount DESC";
        return jdbc.query(sql, (rs,i)-> Map.of(
                "name", rs.getString("name") == null ? "Không xác định" : rs.getString("name"),
                "qty", rs.getLong("qty"),
                "amount", rs.getBigDecimal("amount") == null ? BigDecimal.ZERO : rs.getBigDecimal("amount")
        ));
    }

    private List<Map<String,Object>> topCustomers(int limit){
        // Customer: MaKH, TenKH
        String sql = "SELECT kh.MaKH id, kh.TenKH name, COUNT(dh.MaDH) orders, SUM(dh.TongThanhToan) spend " +
                "FROM Orders dh JOIN Customer kh ON kh.MaKH = dh.MaKH " +
            "JOIN Payment tt ON tt.OrderId = dh.MaDH " +
            "WHERE tt.Status='PAID' " +
                "GROUP BY kh.MaKH, kh.TenKH ORDER BY spend DESC " +
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