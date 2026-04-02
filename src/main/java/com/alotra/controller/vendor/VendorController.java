package com.alotra.controller.vendor;

import com.alotra.dto.OrderDto;
import com.alotra.entity.Order;
import com.alotra.entity.Employee;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.enums.PaymentMethod;
import com.alotra.entity.enums.PaymentStatus;
import com.alotra.repository.OrderRepository;
import com.alotra.service.order.OrderHistoryService;
import com.alotra.service.order.VendorOrderService;
import com.alotra.service.order.OrderHistoryService.OrderItemRow;
import com.alotra.service.order.OrderHistoryService.ItemToppingRow;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alotra.security.EmployeeUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
@RequestMapping("/vendor")
public class VendorController {
    private final VendorOrderService vendorOrderService;
    private final OrderHistoryService orderHistoryService;
    private final OrderRepository orderRepository;

    public VendorController(VendorOrderService vendorOrderService,
                            OrderHistoryService orderHistoryService,
                            OrderRepository orderRepository) {
        this.vendorOrderService = vendorOrderService;
        this.orderHistoryService = orderHistoryService;
        this.orderRepository = orderRepository;
    }

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("counts", vendorOrderService.getDashboardCounts());
        model.addAttribute("recent", vendorOrderService.listTodayOrders());
        return "vendor/dashboard";
    }

    @GetMapping("/orders")
    public String listOrders(@RequestParam(required = false) String status,
                             @RequestParam(required = false) String kw,
                             @RequestParam(required = false) Integer limit,
                             @RequestParam(required = false, defaultValue = "list") String from,
                             Model model) {
        model.addAttribute("items", vendorOrderService.listOrders(status, kw, limit));
        model.addAttribute("status", status);
        model.addAttribute("kw", kw);
        model.addAttribute("limit", limit);
        model.addAttribute("from", from);
        return "vendor/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Integer id, Model model) {
        OrderDto order = orderHistoryService.getOrder(id);
        if (order == null) {
            return "redirect:/vendor/orders";
        }
        List<OrderItemRow> items = orderHistoryService.listOrderItems(id);
        Map<Integer, List<ItemToppingRow>> toppings = new HashMap<>();
        for (OrderItemRow it : items) {
            toppings.put(it.id, orderHistoryService.listOrderedToppings(it.id));
        }
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("toppings", toppings);
        model.addAttribute("pageTitle", "Đơn #" + id);
        return "vendor/order-detail";
    }

    @GetMapping("/orders/{id}/invoice")
    public String invoice(@PathVariable Integer id, Model model) {
        OrderDto order = orderHistoryService.getOrder(id);
        if (order == null) {
            return "redirect:/vendor/orders";
        }
        List<OrderItemRow> items = orderHistoryService.listOrderItems(id);
        Map<Integer, List<ItemToppingRow>> toppings = new HashMap<>();
        for (OrderItemRow it : items) {
            toppings.put(it.id, orderHistoryService.listOrderedToppings(it.id));
        }
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("toppings", toppings);
        model.addAttribute("storeName", "AloTra");
        model.addAttribute("storeAddress", "Khu pho 6, P. Linh Trung, TP. Thu Duc, TP. HCM");
        model.addAttribute("storePhone", "1900 1234");
        return "vendor/invoice";
    }

    @PostMapping("/orders/{id}/advance")
    public String advance(@PathVariable Integer id,
                          @RequestParam(required = false) String from,
                          @AuthenticationPrincipal EmployeeUserDetails current) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            if (current != null && order.getEmployee() == null) {
                Employee e = new Employee();
                e.setId(current.getId());
                order.setEmployee(e);
                orderRepository.save(order);
            }
            if (order.getPayment() != null
                    && order.getPayment().getMethod() == PaymentMethod.BANK_TRANSFER
                    && order.getPayment().getStatus() != PaymentStatus.PAID) {
                return redirectFrom(id, from);
            }
            OrderStatus currentSt = order.getStatus();
            OrderStatus next = vendorOrderService.nextStatus(currentSt);
            if (next != null && next != currentSt) {
                vendorOrderService.updateStatus(id, next);
            }
        }
        return redirectFrom(id, from);
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable Integer id,
                         @RequestParam(required = false) String from,
                         @AuthenticationPrincipal EmployeeUserDetails current) {
        Order order = orderRepository.findById(id).orElse(null);
        String currentSt = order != null && order.getStatus() != null ? order.getStatus().name() : null;
        if (order != null) {
            if (current != null && order.getEmployee() == null) {
                Employee e = new Employee();
                e.setId(current.getId());
                order.setEmployee(e);
                orderRepository.save(order);
            }
            if (vendorOrderService.canCancel(currentSt)) {
                vendorOrderService.updateStatus(id, OrderStatus.CANCELLED);
            }
        }
        return redirectFrom(id, from);
    }

    @PostMapping("/orders/{id}/mark-cash-paid")
    public String markCashPaid(@PathVariable Integer id,
                               @RequestParam(required = false) String from,
                               @AuthenticationPrincipal EmployeeUserDetails current) {
        orderRepository.findById(id).ifPresent(order -> {
            if (order.getPayment() != null && order.getPayment().getStatus() != PaymentStatus.PAID) {
                order.getPayment().setStatus(PaymentStatus.PAID);
                order.getPayment().setPaidAt(LocalDateTime.now());
                if (current != null && order.getEmployee() == null) {
                    Employee e = new Employee();
                    e.setId(current.getId());
                    order.setEmployee(e);
                }
                orderRepository.save(order);
            }
        });
        return redirectFrom(id, from);
    }

    private String redirectFrom(Integer id, String from) {
        if ("detail".equalsIgnoreCase(from)) {
            return "redirect:/vendor/orders/" + id;
        }
        return "redirect:/vendor/orders";
    }
}
