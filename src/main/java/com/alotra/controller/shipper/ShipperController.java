package com.alotra.controller.shipper;

import com.alotra.entity.Order;
import com.alotra.entity.Employee;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.enums.PaymentStatus;
import com.alotra.repository.OrderRepository;
import com.alotra.security.EmployeeUserDetails;
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
    private final OrderRepository orderRepository;

    public ShipperController(ShipperOrderService shipperOrderService,
                            OrderHistoryService orderHistoryService,
                            OrderRepository orderRepository) {
        this.shipperOrderService = shipperOrderService;
        this.orderHistoryService = orderHistoryService;
        this.orderRepository = orderRepository;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal Object principal, Model model) {
        Integer shipperId = getShipperId(principal);
        if (shipperId == null) {
            model.addAttribute("error", "Bạn cần đăng nhập với tài khoản Shipper để truy cập trang này.");
            model.addAttribute("stats", createEmptyStats());
            model.addAttribute("shippingOrders", List.of());
            model.addAttribute("pageTitle", "Kênh Giao Hàng - Bảng điều khiển");
            return "shipper/dashboard";
        }
        
        Map<String, Object> stats = shipperOrderService.getDashboardStats(shipperId);
        model.addAttribute("stats", stats);
        
        List<ShipperOrderService.OrderDto> shippingOrders = shipperOrderService.getTodayShippingOrders(shipperId);
        model.addAttribute("shippingOrders", shippingOrders);
        
        model.addAttribute("pageTitle", "Kênh Giao Hàng - Bảng điều khiển");
        return "shipper/dashboard";
    }

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

    @PostMapping("/orders/{id}/accept")
    public String acceptOrder(@PathVariable Integer id,
                             @AuthenticationPrincipal Object principal,
                             @RequestParam(required = false) String from,
                             RedirectAttributes ra) {
        Integer shipperId = getShipperId(principal);
        if (shipperId == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập với tài khoản Shipper.");
            return "redirect:/shipper/available-orders";
        }
        
        boolean success = shipperOrderService.acceptOrder(id, shipperId);
        
        if (success) {
            ra.addFlashAttribute("message", "Đã nhận đơn hàng #" + id + " thành công!");
            return "redirect:/shipper/orders/" + id;
        } else {
            ra.addFlashAttribute("error", "Không thể nhận đơn hàng này (có thể đã có người nhận).");
            return "redirect:/shipper/available-orders";
        }
    }

    @PostMapping("/orders/{id}/advance")
    public String advanceOrder(@PathVariable Integer id,
                                 @AuthenticationPrincipal Object principal,
                                 @RequestParam(required = false) String from,
                                 RedirectAttributes ra) {
        Integer shipperId = getShipperId(principal);
        if (shipperId == null) {
             ra.addFlashAttribute("error", "Bạn cần đăng nhập với tài khoản Shipper.");
             return "redirect:/shipper/orders";
        }

        boolean success = shipperOrderService.advanceOrder(id, shipperId);

        if (success) {
            ra.addFlashAttribute("message", "Đã chuyển trạng thái đơn hàng #" + id);
        } else {
            ra.addFlashAttribute("error", "Không thể chuyển trạng thái đơn hàng này.");
        }

        return redirectFrom(id, from);
    }

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
        
        OrderRow order = orderHistoryService.getOrder(id);
        if (order == null) {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return "redirect:/shipper/orders";
        }
        
        List<OrderItemRow> items = orderHistoryService.listOrderItems(id);
        Map<Integer, List<ItemToppingRow>> toppings = new HashMap<>();
        for (OrderItemRow item : items) {
            toppings.put(item.id, orderHistoryService.listOrderedToppings(item.id));
        }
        
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("toppings", toppings);
        model.addAttribute("shipperId", shipperId);
        model.addAttribute("pageTitle", "Chi tiết đơn #" + id);
        
        return "shipper/order-detail";
    }

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
        
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null && order.getStatus() == OrderStatus.DELIVERING) {
            if (order.getEmployee() == null) {
                Employee e = new Employee();
                e.setId(shipperId);
                order.setEmployee(e);
            }
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
            ra.addFlashAttribute("message", "Đã cập nhật trạng thái đơn hàng thành công!");
        } else {
            ra.addFlashAttribute("error", "Không thể cập nhật trạng thái đơn hàng.");
        }
        
        return redirectFrom(id, from);
    }

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
        
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return "redirect:/shipper/orders";
        }
        
        if (order.getPayment().getStatus() != PaymentStatus.PAID) {
            order.getPayment().setStatus(PaymentStatus.PAID);
            order.getPayment().setPaidAt(java.time.LocalDateTime.now());
            orderRepository.save(order);
            ra.addFlashAttribute("message", "Đã xác nhận thu tiền từ khách.");
        }
        
        return redirectFrom(id, from);
    }
    
    private Integer getShipperId(Object principal) {
        if (principal instanceof EmployeeUserDetails) {
            EmployeeUserDetails userDetails = (EmployeeUserDetails) principal;
            boolean isShipper = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_SHIPPER".equals(auth.getAuthority()));
            if (isShipper) {
                return userDetails.getId();
            }
        }
        return null;
    }
    
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
