package com.alotra.service.command;

import org.springframework.jdbc.core.JdbcTemplate;

public class UpdateOrderStatusCommand implements AdminCommand {
    private final JdbcTemplate jdbc;
    private final Integer orderId;
    private final String newStatus;
    
    private String previousStatus;

    public UpdateOrderStatusCommand(JdbcTemplate jdbc, Integer orderId, String newStatus) {
        this.jdbc = jdbc;
        this.orderId = orderId;
        this.newStatus = newStatus;
    }

    @Override
    public void execute() {
        this.previousStatus = jdbc.queryForObject("SELECT TrangThaiDonHang FROM Orders WHERE MaDH = ?", String.class, orderId);
        jdbc.update("UPDATE Orders SET TrangThaiDonHang = ? WHERE MaDH = ?", newStatus, orderId);
    }

    @Override
    public void undo() {
        if (this.previousStatus != null) {
            jdbc.update("UPDATE Orders SET TrangThaiDonHang = ? WHERE MaDH = ?", previousStatus, orderId);
        }
    }

    @Override
    public String getDescription() {
        return "Cập nhật đơn hàng #" + orderId + ": " + (previousStatus != null ? previousStatus : "???") + " -> " + newStatus;
    }
}
