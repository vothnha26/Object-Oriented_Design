package com.alotra.controller.shipper;

import com.alotra.entity.DonHang;
import com.alotra.entity.NhanVien;
import com.alotra.repository.DonHangRepository;
import com.alotra.security.NhanVienUserDetails;
import com.alotra.service.OrderHistoryService;
import com.alotra.service.OrderHistoryService.ItemToppingRow;
import com.alotra.service.OrderHistoryService.OrderItemRow;
import com.alotra.service.OrderHistoryService.OrderRow;
import com.alotra.service.ShipperOrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shipper")
public class ShipperController {
    private final ShipperOrderService shipperOrderService;
    private final OrderHistoryService orderHistoryService;
    private final DonHangRepository donHangRepository;

    public ShipperController(ShipperOrderService shipperOrderService,
                            OrderHistoryService orderHistoryService,
                            DonHangRepository donHangRepository) {
        this.shipperOrderService = shipperOrderService;
        this.orderHistoryService = orderHistoryService;
        this.donHangRepository = donHangRepository;
    }

    /**
     * Dashboard - Trang chủ của shipper
     */
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal Object principal, Model model) {
        // Kiểm tra và lấy shipperId
        Integer shipperId = getShipperId(principal);
        if (shipperId == null) {
            model.addAttribute("error", "Bạn cần đăng nhập với tài khoản Shipper để truy cập trang này.");
            model.addAttribute("stats", createEmptyStats());
            model.addAttribute("shippingOrders", List.of());
            model.addAttribute("pageTitle", "Kênh Giao Hàng - Bảng điều khiển");
            return "shipper/dashboard";
        }
        
        // Lấy thống kê
        Map<String, Object> stats = shipperOrderService.getDashboardStats(shipperId);
        model.addAttribute("stats", stats);
        
        // Lấy đơn đang giao hôm nay
        List<ShipperOrderService.OrderDto> shippingOrders = shipperOrderService.getTodayShippingOrders(shipperId);
        model.addAttribute("shippingOrders", shippingOrders);
        
        model.addAttribute("pageTitle", "Kênh Giao Hàng - Bảng điều khiển");
        return "shipper/dashboard";
    }

    /**
     * Danh sách đơn hàng được phân công
     */
    @GetMapping("/orders")
    public String listOrders(@AuthenticationPrincipal Object principal,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String kw,
                            @RequestParam(required = false, defaultValue = "50") Integer limit,
                            Model model) {
        Integer shipperId = getShipperId(principal);
        if (shipperId == null) {
            model.addAttribute("error", "Bạn cần đăng nhập với tài khoản Shipper để xem đơn hàng.");
            model.addAttribute("orders", List.of());
            model.addAttribute("pageTitle", "Đơn hàng được phân công");
            return "shipper/orders";
        }
        
        List<ShipperOrderService.OrderDto> orders = shipperOrderService.getAssignedOrders(shipperId, status, kw, limit);
        
        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        model.addAttribute("kw", kw);
        model.addAttribute("limit", limit);
        model.addAttribute("pageTitle", "Đơn hàng được phân công");
        
        return "shipper/orders";
    }

    /**
     * Danh sách đơn hàng chưa có người nhận (ChoXuLy)
     */
    @GetMapping("/available-orders")
    public String availableOrders(@AuthenticationPrincipal Object principal,
                                  @RequestParam(required = false) String kw,
                                  @RequestParam(required = false, defaultValue = "50") Integer limit,
                                  Model model) {
        Integer shipperId = getShipperId(principal);
        if (shipperId == null) {
            model.addAttribute("error", "Bạn cần đăng nhập với tài khoản Shipper.");
            model.addAttribute("orders", List.of());
            model.addAttribute("pageTitle", "Đơn hàng chờ nhận");
            return "shipper/available-orders";
        }
        
        List<ShipperOrderService.OrderDto> orders = shipperOrderService.getAvailableOrders(kw, limit);
        
        model.addAttribute("orders", orders);
        model.addAttribute("kw", kw);
        model.addAttribute("limit", limit);
        model.addAttribute("pageTitle", "Đơn hàng chờ nhận");
        
        return "shipper/available-orders";
    }

    /**
     * Nhận đơn hàng (assign vào shipper)
     */
    @PostMapping("/orders/{id}/accept")
    public String acceptOrder(@PathVariable Integer id,
                             @AuthenticationPrincipal Object principal,
                             @RequestParam(required = false) String from,
                             RedirectAttributes ra) {
        System.out.println("=== DEBUG: acceptOrder called with id=" + id);
        
        Integer shipperId = getShipperId(principal);
        if (shipperId == null) {
            System.out.println("=== DEBUG: shipperId is NULL");
            ra.addFlashAttribute("error", "Bạn cần đăng nhập với tài khoản Shipper.");
            return "redirect:/shipper/available-orders";
        }
        
        System.out.println("=== DEBUG: shipperId=" + shipperId + ", calling service...");
        boolean success = shipperOrderService.acceptOrder(id, shipperId);
        System.out.println("=== DEBUG: service returned success=" + success);
        
        if (success) {
            ra.addFlashAttribute("message", "Đã nhận đơn hàng #" + id + " thành công!");
            return "redirect:/shipper/orders/" + id;
        } else {
            ra.addFlashAttribute("error", "Không thể nhận đơn hàng này (có thể đã có người nhận).");
            return "redirect:/shipper/available-orders";
        }
    }

    /**
     * Chuyển bước tiếp theo của đơn hàng
     */
    @PostMapping("/orders/{id}/advance")
    public String advanceOrder(@PathVariable Integer id,
                                 @AuthenticationPrincipal Object principal,
                                 @RequestParam(required = false) String from,
                                 RedirectAttributes ra) {
        System.out.println("=== CONTROLLER: advanceOrder called with id=" + id); // Thêm log

        Integer shipperId = getShipperId(principal);
        if (shipperId == null) { /* ... xử lý lỗi ... */ }

        // === Sửa: Gọi advanceOrder thay vì advanceOrderSimple ===
        boolean success = shipperOrderService.advanceOrder(id, shipperId);
        // ======================================================

        if (success) { /* ... */ } else { /* ... */ }

        return redirectFrom(id, from);
    }

    /**
     * Hủy đơn hàng
     */
    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Integer id,
                             @AuthenticationPrincipal Object principal,
                             @RequestParam(required = false) String from,
                             RedirectAttributes ra) {
        Integer shipperId = getShipperId(principal);
        if (shipperId == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập với tài khoản Shipper.");
            return "redirect:/shipper/orders";
        }
        
        boolean success = shipperOrderService.cancelOrder(id, shipperId);
        
        if (success) {
            ra.addFlashAttribute("message", "Đã hủy đơn hàng #" + id);
        } else {
            ra.addFlashAttribute("error", "Không thể hủy đơn hàng này (chỉ được hủy khi ChoXuLy hoặc DangPhaChe).");
        }
        
        return redirectFrom(id, from);
    }

    /**
     * Chi tiết đơn hàng
     */
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Integer id,
                             @AuthenticationPrincipal Object principal,
                             Model model,
                             RedirectAttributes ra) {
        Integer shipperId = getShipperId(principal);
        if (shipperId == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập với tài khoản Shipper.");
            return "redirect:/shipper/orders";
        }
        
        // Lấy thông tin đơn hàng (không cần kiểm tra quyền)
        OrderRow order = orderHistoryService.getOrder(id);
        if (order == null) {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return "redirect:/shipper/orders";
        }
        
        // Lấy chi tiết items và toppings
        List<OrderItemRow> items = orderHistoryService.listOrderItems(id);
        Map<Integer, List<ItemToppingRow>> toppings = new HashMap<>();
        for (OrderItemRow item : items) {
            toppings.put(item.id, orderHistoryService.listOrderItemToppings(item.id));
        }
        
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("toppings", toppings);
        model.addAttribute("shipperId", shipperId);
        model.addAttribute("pageTitle", "Chi tiết đơn #" + id);
        
        return "shipper/order-detail";
    }

    /**
     * Đánh dấu đơn hàng đã giao thành công
     */
    @PostMapping("/orders/{id}/mark-delivered")
    public String markDelivered(@PathVariable Integer id,
                               @AuthenticationPrincipal Object principal,
                               @RequestParam(required = false) String from,
                               RedirectAttributes ra) {
        Integer shipperId = getShipperId(principal);
        if (shipperId == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập với tài khoản Shipper.");
            return "redirect:/shipper/orders";
        }
        
        // Đơn giản: chỉ cần chuyển sang DaGiao
        DonHang order = donHangRepository.findById(id).orElse(null);
        if (order != null && "DangGiao".equals(order.getStatus())) {
            // Tự động gán shipper nếu chưa có
            if (order.getEmployee() == null) {
                order.setEmployee(new NhanVien());
                order.getEmployee().setId(shipperId);
            }
            order.setStatus("DaGiao");
            donHangRepository.save(order);
            ra.addFlashAttribute("message", "Đã cập nhật trạng thái đơn hàng thành công!");
        } else {
            ra.addFlashAttribute("error", "Không thể cập nhật trạng thái đơn hàng.");
        }
        
        return redirectFrom(id, from);
    }

    /**
     * Xác nhận đã thu tiền từ khách (COD)
     */
    @PostMapping("/orders/{id}/confirm-payment")
    public String confirmPayment(@PathVariable Integer id,
                                @AuthenticationPrincipal Object principal,
                                @RequestParam(required = false) String from,
                                RedirectAttributes ra) {
        Integer shipperId = getShipperId(principal);
        if (shipperId == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập với tài khoản Shipper.");
            return "redirect:/shipper/orders";
        }
        
        DonHang order = donHangRepository.findById(id).orElse(null);
        if (order == null) {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return "redirect:/shipper/orders";
        }
        
        if (!"DaThanhToan".equals(order.getPaymentStatus())) {
            order.setPaymentStatus("DaThanhToan");
            order.setPaidAt(java.time.LocalDateTime.now());
            donHangRepository.save(order);
            ra.addFlashAttribute("message", "Đã xác nhận thu tiền từ khách.");
        }
        
        return redirectFrom(id, from);
    }
    
    /**
     * Helper method to get shipper ID from principal
     */
    private Integer getShipperId(Object principal) {
        if (principal instanceof NhanVienUserDetails) {
            NhanVienUserDetails userDetails = (NhanVienUserDetails) principal;
            // Chỉ cho phép nếu là SHIPPER thật (role = 3)
            boolean isShipper = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_SHIPPER".equals(auth.getAuthority()));
            if (isShipper) {
                return userDetails.getId();
            }
        }
        return null;
    }
    
    /**
     * Create empty stats for display when no shipper logged in
     */
    private Map<String, Object> createEmptyStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("shipping", 0L);
        stats.put("deliveredToday", 0L);
        stats.put("totalAssigned", 0L);
        stats.put("deliveredThisWeek", 0L);
        return stats;
    }

    private String redirectFrom(Integer id, String from) {
        if ("detail".equalsIgnoreCase(from)) {
            return "redirect:/shipper/orders/" + id;
        }
        return "redirect:/shipper/orders";
    }
}
