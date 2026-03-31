package com.alotra.controller.account;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alotra.entity.Order;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.enums.PaymentMethod;
import com.alotra.entity.enums.PaymentStatus;
import com.alotra.repository.OrderRepository;
import com.alotra.security.CustomerUserDetails;
import com.alotra.service.OrderHistoryService;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    @org.springframework.beans.factory.annotation.Value("${payment.settle.bank-code:VCB}")
    private String BANK_CODE;
    @org.springframework.beans.factory.annotation.Value("${payment.settle.account:}")
    private String ACCOUNT_NUMBER;
    @org.springframework.beans.factory.annotation.Value("${payment.settle.account-name:}")
    private String ACCOUNT_NAME;
    private static final int EXPIRY_MINUTES = 30;

    private final OrderRepository orderRepo;
    private final OrderHistoryService customerOrderService;

    public PaymentController(OrderRepository orderRepo, OrderHistoryService customerOrderService) {
        this.orderRepo = orderRepo;
        this.customerOrderService = customerOrderService;
    }

    private boolean isTransferMethod(Object method) {
        return method == PaymentMethod.BANK_TRANSFER;
    }

    @GetMapping("/{id}")
    public String showPaymentPage(@PathVariable Integer id,
                                  @AuthenticationPrincipal CustomerUserDetails principal,
                                  Model model) {
        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) return "redirect:/cart?error=Đơn hàng không tồn tại";
        if (principal == null || order.getCustomer() == null || !java.util.Objects.equals(order.getCustomer().getId(), principal.getId())) {
            return "redirect:/account/orders";
        }
        if (!isTransferMethod(order.getPayment().getMethod())) {
            return "redirect:/account/orders";
        }
        if (order.getPayment().getStatus() == PaymentStatus.PAID) {
            return "redirect:/payment/" + id + "/success";
        }
        
        var header = customerOrderService.getOrder(id);
        var items = customerOrderService.listOrderItems(id);
        Map<Integer, List<OrderHistoryService.ItemToppingRow>> toppings = new HashMap<>();
        for (var it : items) toppings.put(it.id, customerOrderService.listOrderedToppings(it.id));

        String addInfo = "ALOTRA DH " + id;
        String qrUrl = buildVietQrUrl(BANK_CODE, ACCOUNT_NUMBER, order.getTotalAmount().intValue(), addInfo);
        
        LocalDateTime created = order.getCreatedAt();
        LocalDateTime expiry = (created != null ? created : LocalDateTime.now()).plusMinutes(EXPIRY_MINUTES);
        long expiryEpochMillis = expiry.toInstant(ZoneOffset.UTC).toEpochMilli();

        model.addAttribute("order", header);
        model.addAttribute("items", items);
        model.addAttribute("toppings", toppings);
        model.addAttribute("qrUrl", qrUrl);
        model.addAttribute("addInfo", addInfo);
        model.addAttribute("bankCode", BANK_CODE);
        model.addAttribute("accountNumber", ACCOUNT_NUMBER);
        model.addAttribute("expiryEpochMillis", expiryEpochMillis);
        model.addAttribute("expiryMinutes", EXPIRY_MINUTES);
        if (ACCOUNT_NAME != null && !ACCOUNT_NAME.isBlank()) {
            model.addAttribute("accountName", ACCOUNT_NAME);
        }
        model.addAttribute("pageTitle", "Thanh toán đơn #" + id);
        return "payment/transfer";
    }

    @GetMapping("/{id}/status")
    @ResponseBody
    public Map<String, Object> getStatus(@PathVariable Integer id,
                                         @AuthenticationPrincipal CustomerUserDetails principal) {
        Map<String,Object> m = new HashMap<>();
        var opt = orderRepo.findById(id);
        if (opt.isPresent()) {
            Order order = opt.get();
            if (isTransferMethod(order.getPayment().getMethod()) && order.getPayment().getStatus() != PaymentStatus.PAID) {
                LocalDateTime expiry = (order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now()).plusMinutes(EXPIRY_MINUTES);
                if (LocalDateTime.now().isAfter(expiry) && order.getStatus() != OrderStatus.CANCELLED) {
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepo.save(order);
                }
            }
        }
        String status = opt.map(o -> o.getPayment().getStatus() != null ? o.getPayment().getStatus().name() : "NA").orElse("NA");
        String orderStatus = opt.map(o -> o.getStatus() != null ? o.getStatus().name() : "NA").orElse("NA");
        m.put("paymentStatus", status);
        m.put("orderStatus", orderStatus);
        return m;
    }

    @PostMapping("/{id}/admin/mark-paid")
    public ResponseEntity<?> adminMarkPaid(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        }
        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();
        order.getPayment().setStatus(PaymentStatus.PAID);
        order.getPayment().setPaidAt(LocalDateTime.now());
        orderRepo.save(order);
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @GetMapping("/{id}/success")
    public String success(@PathVariable Integer id,
                          @AuthenticationPrincipal CustomerUserDetails principal,
                          Model model) {
        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) return "redirect:/account/orders";
        if (principal == null || order.getCustomer() == null || !java.util.Objects.equals(order.getCustomer().getId(), principal.getId())) {
            return "redirect:/account/orders";
        }
        model.addAttribute("orderId", id);
        return "payment/success";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Integer id,
                         @AuthenticationPrincipal CustomerUserDetails principal,
                         RedirectAttributes ra) {
        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) {
            ra.addFlashAttribute("error", "Đơn hàng không tồn tại");
            return "redirect:/account/orders";
        }
        if (principal == null || order.getCustomer() == null || !java.util.Objects.equals(order.getCustomer().getId(), principal.getId())) {
            return "redirect:/account/orders";
        }
        if (order.getPayment().getStatus() == PaymentStatus.PAID) {
            return "redirect:/payment/" + id + "/success";
        }
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepo.save(order);
            ra.addFlashAttribute("msg", "Đã hủy đơn #" + id);
        } else {
            ra.addFlashAttribute("error", "Chỉ hủy được đơn đang chờ xử lý.");
        }
        return "redirect:/account/orders";
    }

    private String buildVietQrUrl(String bankCode, String accountNumber, int amount, String addInfo) {
        String info = URLEncoder.encode(addInfo, StandardCharsets.UTF_8);
        return "https://img.vietqr.io/image/" + bankCode + "-" + accountNumber + "-print.png?amount=" + amount + "&addInfo=" + info;
    }
}