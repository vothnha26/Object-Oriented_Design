package com.alotra.service;

import com.alotra.entity.enums.OrderStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import com.alotra.service.command.AdminCommand;
import com.alotra.service.command.AdminCommandInvoker;
import com.alotra.service.command.UpdateOrderStatusCommand;
import org.springframework.stereotype.Service;

import com.alotra.service.query.AbstractOrderQuery;
import com.alotra.service.query.OrderFilterStrategy;
import com.alotra.service.query.StatusOrderFilter;
import com.alotra.repository.OrderRepository;
import com.alotra.dto.OrderDto;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VendorOrderService {
    private static final ZoneId HCM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final JdbcTemplate jdbc;
    private final AdminCommandInvoker commandInvoker;
    private final OrderRepository orderRepository;

    public VendorOrderService(JdbcTemplate jdbc, AdminCommandInvoker commandInvoker, OrderRepository orderRepository) {
        this.jdbc = jdbc;
        this.commandInvoker = commandInvoker;
        this.orderRepository = orderRepository;
    }

    public Map<String, Object> getDashboardCounts() {
        Map<String, Object> m = new HashMap<>();
        m.put("pending", countByStatus(OrderStatus.PENDING.name()));
        m.put("preparing", countByStatus(OrderStatus.PREPARING.name()));
        m.put("shipping", countByStatus(OrderStatus.DELIVERING.name()));

        // Today orders (MySQL syntax)
        String sqlToday = "SELECT COUNT(*) FROM Orders WHERE DATE(NgayLap) = CURDATE()";
        Integer today = jdbc.queryForObject(sqlToday, Integer.class);
        m.put("today", today == null ? 0 : today);
        return m;
    }

    public int countByStatus(String status) {
        // Table: Orders, Column: TrangThaiDonHang
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM Orders WHERE TrangThaiDonHang = ?", Integer.class,
                status);
        return n == null ? 0 : n;
    }

    public List<OrderDto> listOrders(String status, String kw, Integer limit) {
        OrderStatus targetStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                targetStatus = OrderStatus.valueOf(status);
            } catch (Exception ignored) {
            }
        }

        final OrderStatus finalStatus = targetStatus;
        AbstractOrderQuery query = new AbstractOrderQuery(orderRepository) {
            @Override
            protected OrderFilterStrategy getFilter() {
                if (finalStatus != null) {
                    return new StatusOrderFilter(finalStatus);
                }
                // If no specific status, return all orders by using a dummy filter
                return o -> true;
            }
        };

        return query.execute(kw, limit != null && limit > 0 ? limit : 50);
    }

    public void updateStatus(Integer id, String newStatus) {
        AdminCommand cmd = new UpdateOrderStatusCommand(jdbc, id, newStatus);
        commandInvoker.execute(cmd);
    }

    public void updateStatus(Integer id, OrderStatus newStatus) {
        updateStatus(id, newStatus.name());
    }

    public OrderStatus nextStatus(OrderStatus current) {
        if (current == null)
            return OrderStatus.PENDING;
        return switch (current) {
            case PENDING -> OrderStatus.PREPARING;
            case PREPARING -> OrderStatus.DELIVERING;
            case DELIVERING -> OrderStatus.DELIVERED;
            default -> current;
        };
    }

    public boolean canCancel(String current) {
        if (current == null)
            return false;
        return OrderStatus.PENDING.name().equals(current) || OrderStatus.PREPARING.name().equals(current);
    }

    public List<OrderDto> listTodayOrders() {
        AbstractOrderQuery query = new AbstractOrderQuery(orderRepository) {
            @Override
            protected OrderFilterStrategy getFilter() {
                return o -> {
                    if (o.getCreatedAt() == null)
                        return false;
                    java.time.LocalDate orderDate = o.getCreatedAt().atZone(HCM_ZONE).toLocalDate();
                    java.time.LocalDate today = java.time.LocalDate.now(HCM_ZONE);
                    return orderDate.equals(today);
                };
            }
        };
        return query.execute(null, null);
    }
}