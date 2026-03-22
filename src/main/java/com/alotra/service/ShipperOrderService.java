package com.alotra.service;

import com.alotra.entity.DonHang;
import com.alotra.entity.NhanVien;
import com.alotra.repository.DonHangRepository;
import com.alotra.repository.NhanVienRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShipperOrderService {
    private final DonHangRepository donHangRepository;
    private final NhanVienRepository nhanVienRepository;

    public ShipperOrderService(DonHangRepository donHangRepository, NhanVienRepository nhanVienRepository) {
        this.donHangRepository = donHangRepository;
        this.nhanVienRepository = nhanVienRepository;
    }

    /**
     * Lấy thống kê cho dashboard của shipper (ĐÃ SỬA LỖI)
     */
    public Map<String, Object> getDashboardStats(Integer shipperId) {
        Map<String, Object> stats = new HashMap<>();

        // Xác định các mốc thời gian
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        // Tính mốc 7 ngày trước (ví dụ: nếu hôm nay là thứ 3, lấy từ thứ 3 tuần trước)
        LocalDateTime startOfWeek = LocalDateTime.of(LocalDate.now().minusDays(6), LocalTime.MIN); // 7 ngày bao gồm hôm nay

        // === BẮT ĐẦU SỬA LỖI: Gọi đúng các hàm Repository ===

        // 1. Đang giao (Chỉ của shipper này)
        long shipping = donHangRepository.countByEmployeeIdAndStatus(shipperId, "DangGiao");

        // 2. Đã giao hôm nay (Chỉ của shipper này)
        long deliveredToday = donHangRepository.countByEmployeeIdAndStatusAndCreatedAtBetween(
                shipperId, "DaGiao", startOfDay, endOfDay);

        // 3. Tổng được phân công (Các đơn shipper này đang xử lý, bao gồm cả đơn vừa nhận)
        List<String> assignedStatuses = List.of("ChoXuLy", "DangPhaChe", "DangGiao");
        long totalAssigned = donHangRepository.countByEmployeeIdAndStatusIn(shipperId, assignedStatuses);

        // 4. Giao tuần này (Tính 7 ngày gần nhất, chỉ của shipper này)
        long deliveredThisWeek = donHangRepository.countByEmployeeIdAndStatusAndCreatedAtAfter(
                shipperId, "DaGiao", startOfWeek);
        
        // === KẾT THÚC SỬA LỖI ===

        stats.put("shipping", shipping);
        stats.put("deliveredToday", deliveredToday);
        stats.put("totalAssigned", totalAssigned);
        stats.put("deliveredThisWeek", deliveredThisWeek);
        
        return stats;
    }

    /**
     * Lấy danh sách đơn hàng (TẤT CẢ, không phân biệt shipper)
     */
    public List<OrderDto> getAssignedOrders(Integer shipperId, String status, String keyword, Integer limit) {
    	List<String> targetStatuses;
        // Xác định các trạng thái cần lấy
        if (status != null && !status.isBlank()) {
            // Nếu có lọc status cụ thể
            if ("DaHuy".equals(status)) { // Tránh lấy đơn đã hủy trừ khi được yêu cầu rõ
                 return List.of(); // Hoặc trả về đơn hủy nếu cần
            }
            targetStatuses = List.of(status);
        } else {
            // Mặc định lấy các trạng thái liên quan đến shipper (trừ hủy)
            targetStatuses = List.of("ChoXuLy", "DangPhaChe", "DangGiao", "DaGiao");
        }
        
     // === Sử dụng query trong Repository (Cách tốt nhất) ===
        // List<DonHang> orders = donHangRepository.findByEmployeeIdAndStatusIn(shipperId, targetStatuses);
        // Tạm thời filter trong code:
        List<DonHang> orders = donHangRepository.findAll().stream()
                .filter(o -> o.getEmployee() != null && shipperId.equals(o.getEmployee().getId())) // LỌC THEO SHIPPER ID
                .filter(o -> targetStatuses.contains(o.getStatus())) // Lọc theo trạng thái mong muốn
             // === BẮT ĐẦU SỬA: Lọc bỏ đơn đã hoàn thành ===
                .filter(o -> !(
                    "DaGiao".equals(o.getStatus()) && "DaThanhToan".equals(o.getPaymentStatus())
                ))
                // === KẾT THÚC SỬA ===
                .collect(Collectors.toList());
        
        // Filter by keyword
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            orders = orders.stream()
                .filter(o -> {
                    String custName = o.getCustomer() != null && o.getCustomer().getFullName() != null 
                        ? o.getCustomer().getFullName().toLowerCase() : "";
                    String phone = o.getReceiverPhone() != null ? o.getReceiverPhone() : "";
                    String address = o.getShippingAddress() != null ? o.getShippingAddress().toLowerCase() : "";
                    return custName.contains(kw) || phone.contains(kw) || address.contains(kw);
                })
                .collect(Collectors.toList());
        }
        
        // Sort by created time desc
        orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        // Apply limit
        if (limit != null && limit > 0 && orders.size() > limit) {
            orders = orders.subList(0, limit);
        }
        
        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Lấy danh sách đơn hàng chờ xử lý (chưa có shipper)
     */
    public List<OrderDto> getAvailableOrders(String keyword, Integer limit) {
    	List<DonHang> orders = donHangRepository.findAll().stream()
                .filter(o -> "ChoXuLy".equals(o.getStatus()) && o.getEmployee() == null) // LOGIC ĐÚNG
                .collect(Collectors.toList());
        
        // Filter by keyword
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            orders = orders.stream()
                .filter(o -> {
                    String custName = o.getCustomer() != null && o.getCustomer().getFullName() != null 
                        ? o.getCustomer().getFullName().toLowerCase() : "";
                    String phone = o.getReceiverPhone() != null ? o.getReceiverPhone() : "";
                    String address = o.getShippingAddress() != null ? o.getShippingAddress().toLowerCase() : "";
                    return custName.contains(kw) || phone.contains(kw) || address.contains(kw);
                })
                .collect(Collectors.toList());
        }
        
        // Sort by created time desc
        orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        // Apply limit
        if (limit != null && limit > 0 && orders.size() > limit) {
            orders = orders.subList(0, limit);
        }
        
        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Lấy đơn hàng đang giao (TẤT CẢ, không giới hạn ngày)
     */
    public List<OrderDto> getTodayShippingOrders(Integer shipperId) {
        List<DonHang> orders = donHangRepository.findAll().stream()
            .filter(o -> "DangGiao".equals(o.getStatus()))
            .collect(Collectors.toList());
        
        orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Cập nhật trạng thái đơn hàng (shipper chỉ có thể chuyển từ DangGiao -> DaGiao)
     */
    @Transactional
    public boolean markAsDelivered(Integer orderId, Integer shipperId) {
        DonHang order = donHangRepository.findById(orderId).orElse(null);
        if (order == null) return false;
        
        // Kiểm tra đơn có được phân cho shipper này không
        if (order.getEmployee() == null || !order.getEmployee().getId().equals(shipperId)) {
            return false;
        }
        
        // Chỉ cho phép chuyển từ DangGiao -> DaGiao
        if (!"DangGiao".equals(order.getStatus())) {
            return false;
        }
        
        order.setStatus("DaGiao");
        donHangRepository.save(order);
        return true;
    }

    /**
     * Shipper nhận đơn hàng (assign shipper vào đơn)
     */
    @Transactional
    public boolean acceptOrder(Integer orderId, Integer shipperId) {
        System.out.println("=== SERVICE DEBUG: acceptOrder - orderId=" + orderId + ", shipperId=" + shipperId);
        
        DonHang order = donHangRepository.findById(orderId).orElse(null);
        if (order == null) {
            System.out.println("=== SERVICE DEBUG: Order NOT FOUND");
            return false;
        }
        
        System.out.println("=== SERVICE DEBUG: Order found, current employee=" + 
            (order.getEmployee() != null ? order.getEmployee().getId() : "NULL"));
        
        // Chỉ nhận đơn chưa có shipper hoặc đang ChoXuLy
        if (order.getEmployee() != null) {
            System.out.println("=== SERVICE DEBUG: Order already has employee");
            return false; // Đã có shipper khác
        }
        
        // Load shipper entity từ database
        NhanVien shipper = nhanVienRepository.findById(shipperId).orElse(null);
        if (shipper == null) {
            System.out.println("=== SERVICE DEBUG: Shipper NOT FOUND in database");
            return false;
        }
        
        System.out.println("=== SERVICE DEBUG: Shipper found: " + shipper.getFullName());
        
        // Gán shipper cho đơn
        order.setEmployee(shipper);
        
        System.out.println("=== SERVICE DEBUG: Saving order...");
        donHangRepository.save(order);
        System.out.println("=== SERVICE DEBUG: Order saved successfully");
        return true;
    }

    /**
     * Chuyển bước tiếp theo của đơn hàng (như Vendor)
     */
    @Transactional
    public boolean advanceOrder(Integer orderId, Integer shipperId) {
        DonHang order = donHangRepository.findById(orderId).orElse(null);
        if (order == null) return false;
        
        // Kiểm tra quyền: phải là shipper của đơn này
        if (order.getEmployee() == null || !shipperId.equals(order.getEmployee().getId())) {
            System.out.println("=== SERVICE ADVANCE: Order not assigned to this shipper");
            return false;
        }
        
        // Block nếu chưa thanh toán mà là chuyển khoản
        if ("ChuyenKhoan".equalsIgnoreCase(order.getPaymentMethod())
                && !"DaThanhToan".equals(order.getPaymentStatus())) {
            System.out.println("=== SERVICE ADVANCE: Blocked - ChuyenKhoan not paid");
            return false;
        }
        
        String currentStatus = order.getStatus();
        
        // === BỎ ĐOẠN CODE BLOCK NÀY ===
        // if ("ChoXuLy".equals(currentStatus)) {
        //     System.out.println("=== SERVICE ADVANCE: Cannot advance from ChoXuLy");
        //     return false;
        // }
        // === KẾT THÚC BỎ ===
        
        String nextStatus = getNextStatus(currentStatus);
        System.out.println("=== SERVICE ADVANCE: current=" + currentStatus + ", next=" + nextStatus);

        if (nextStatus != null && !nextStatus.equals(currentStatus)) {
            order.setStatus(nextStatus);
            donHangRepository.save(order);
            System.out.println("=== SERVICE ADVANCE: Status updated");
            return true;
        }
        System.out.println("=== SERVICE ADVANCE: No status change");
        return false;
    }

    /**
     * Chuyển bước đơn giản - không cần kiểm tra ownership, tự động gán shipper
     */
    @Transactional
    public boolean advanceOrderSimple(Integer orderId, Integer shipperId) {
        System.out.println("=== SERVICE: advanceOrderSimple - orderId=" + orderId + ", shipperId=" + shipperId);
        
        DonHang order = donHangRepository.findById(orderId).orElse(null);
        if (order == null) {
            System.out.println("=== SERVICE: Order NOT FOUND");
            return false;
        }
        
        // Nếu chưa có employee, tự động gán shipper này
        if (order.getEmployee() == null) {
            NhanVien shipper = nhanVienRepository.findById(shipperId).orElse(null);
            if (shipper != null) {
                order.setEmployee(shipper);
                System.out.println("=== SERVICE: Auto-assigned shipper to order");
            }
        }
        
        // Block nếu chưa thanh toán mà là chuyển khoản
        if ("ChuyenKhoan".equalsIgnoreCase(order.getPaymentMethod())
                && !"DaThanhToan".equals(order.getPaymentStatus())) {
            System.out.println("=== SERVICE: Blocked - ChuyenKhoan not paid yet");
            return false;
        }
        
        String currentStatus = order.getStatus();
        String nextStatus = getNextStatus(currentStatus);
        
        System.out.println("=== SERVICE: currentStatus=" + currentStatus + ", nextStatus=" + nextStatus);
        
        if (nextStatus != null && !nextStatus.equals(currentStatus)) {
            order.setStatus(nextStatus);
            donHangRepository.save(order);
            System.out.println("=== SERVICE: Order status updated successfully");
            return true;
        }
        
        System.out.println("=== SERVICE: No status change needed");
        return false;
    }

    /**
     * Hủy đơn hàng (chỉ khi ChoXuLy hoặc DangPhaChe)
     */
    @Transactional
    public boolean cancelOrder(Integer orderId, Integer shipperId) {
        DonHang order = donHangRepository.findById(orderId).orElse(null);
        if (order == null) return false;
        
        // Kiểm tra quyền
        if (order.getEmployee() == null || !order.getEmployee().getId().equals(shipperId)) {
            return false;
        }
        
        String currentStatus = order.getStatus();
        if (canCancel(currentStatus)) {
            order.setStatus("DaHuy");
            donHangRepository.save(order);
            return true;
        }
        
        return false;
    }

    /**
     * Lấy trạng thái tiếp theo
     */
    private String getNextStatus(String currentStatus) {
        if (currentStatus == null) return null;
        switch (currentStatus) {
            case "ChoXuLy": return "DangPhaChe";
            case "DangPhaChe": return "DangGiao";
            case "DangGiao": return "DaGiao";
            default: return null;
        }
    }

    /**
     * Kiểm tra có thể hủy đơn không
     */
    private boolean canCancel(String status) {
        return "ChoXuLy".equals(status) || "DangPhaChe".equals(status);
    }

    /**
     * Kiểm tra xem đơn có thuộc về shipper này không
     */
    public boolean isOrderAssignedToShipper(Integer orderId, Integer shipperId) {
        DonHang order = donHangRepository.findById(orderId).orElse(null);
        if (order == null) return false;
        
        return order.getEmployee() != null && order.getEmployee().getId().equals(shipperId);
    }

    private OrderDto toDto(DonHang o) {
        OrderDto dto = new OrderDto();
        dto.id = o.getId();
        dto.customerName = o.getCustomer() != null ? o.getCustomer().getFullName() : "N/A";
        dto.customerPhone = o.getCustomer() != null ? o.getCustomer().getPhone() : "N/A";
        dto.receiverName = o.getReceiverName();
        dto.receiverPhone = o.getReceiverPhone();
        dto.shippingAddress = o.getShippingAddress();
        dto.createdAt = o.getCreatedAt();
        dto.status = o.getStatus();
        dto.paymentStatus = o.getPaymentStatus();
        dto.paymentMethod = o.getPaymentMethod();
        dto.total = o.getTongThanhToan();
        dto.note = o.getNote();
        dto.receivingMethod = o.getReceivingMethod();
        return dto;
    }

    public static class OrderDto {
        public Integer id;
        public String customerName;
        public String customerPhone;
        public String receiverName;
        public String receiverPhone;
        public String shippingAddress;
        public LocalDateTime createdAt;
        public String status;
        public String paymentStatus;
        public String paymentMethod;
        public java.math.BigDecimal total;
        public String note;
        public String receivingMethod;
    }
}
