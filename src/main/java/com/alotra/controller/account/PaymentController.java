package com.alotra.controller.account;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.alotra.config.SepayConfig;
import com.alotra.entity.Order;
import com.alotra.entity.enums.PaymentMethod;
import com.alotra.entity.enums.PaymentStatus;
import com.alotra.entity.state.OrderContext;
import com.alotra.dto.PaymentRequest;
import com.alotra.dto.PaymentResult;
import com.alotra.payment.SepayPaymentProcessor;
import com.alotra.security.CustomerUserDetails;
import com.alotra.service.order.OrderHistoryService;
import com.alotra.repository.OrderRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    private static final int EXPIRY_MINUTES = 30;

    private final OrderRepository orderRepo;
    private final OrderHistoryService customerOrderService;
    private final SepayPaymentProcessor sepayPaymentProcessor;
    private final SepayConfig sepayConfig;

    public PaymentController(OrderRepository orderRepo, OrderHistoryService customerOrderService,
            SepayPaymentProcessor sepayPaymentProcessor, SepayConfig sepayConfig) {
        this.orderRepo = orderRepo;
        this.customerOrderService = customerOrderService;
        this.sepayPaymentProcessor = sepayPaymentProcessor;
        this.sepayConfig = sepayConfig;
    }

    private boolean isTransferMethod(Object method) {
        return method == PaymentMethod.BANK_TRANSFER;
    }

    @PostMapping("/{id}/initiate-sepay")
    @ResponseBody
    public ResponseEntity<?> initiateSepayPayment(@PathVariable Integer id,
            @AuthenticationPrincipal CustomerUserDetails principal) {
        try {
            Order order = orderRepo.findById(id).orElse(null);
            if (order == null) return ResponseEntity.notFound().build();
            if (principal == null || order.getCustomer() == null ||
                    !java.util.Objects.equals(order.getCustomer().getId(), principal.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            if (order.getPayment() == null || order.getPayment().getStatus() == PaymentStatus.PAID) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order already paid"));
            }

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setOrderId(order.getId());
            paymentRequest.setAmount(order.getFinalTotal());
            paymentRequest.setMethod("SEPAY");

            PaymentResult result = sepayPaymentProcessor.initiateSepayPayment(paymentRequest, order);
            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "redirectUrl", result.getRedirectUrl(),
                        "transactionRef", result.getTransactionRef(),
                        "message", result.getMessage()));
            } else {
                return ResponseEntity.internalServerError().body(Map.of("success", false, "error", result.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{id}")
    public String showPaymentPage(@PathVariable Integer id,
            @AuthenticationPrincipal CustomerUserDetails principal,
            Model model) {
        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) return "redirect:/cart?error=Đơn hàng không tồn tại";
        if (principal == null || order.getCustomer() == null
                || !java.util.Objects.equals(order.getCustomer().getId(), principal.getId())) {
            return "redirect:/account/orders";
        }
        if (!isTransferMethod(order.getPayment().getMethod())) return "redirect:/account/orders";
        if (order.getPayment().getStatus() == PaymentStatus.PAID) return "redirect:/payment/" + id + "/success";

        var header = customerOrderService.getOrder(id);
        var items = customerOrderService.listOrderItems(id);
        Map<Integer, List<OrderHistoryService.ItemToppingRow>> toppings = new HashMap<>();
        for (var it : items) toppings.put(it.id, customerOrderService.listOrderedToppings(it.id));

        String bankCode = sepayConfig.getBankCode();
        String accountNumber = sepayConfig.getBankAccount();
        String accountName = sepayConfig.getBankAccountName();
        String addInfo = "ALOTRA DH " + id;
        
        String qrUrl = buildVietQrUrl(bankCode, accountNumber, order.getFinalTotal().intValue(), addInfo);

        LocalDateTime created = order.getCreatedAt();
        LocalDateTime expiry = (created != null ? created : LocalDateTime.now()).plusMinutes(EXPIRY_MINUTES);
        long expiryEpochMillis = expiry.toInstant(ZoneOffset.UTC).toEpochMilli();

        model.addAttribute("order", header);
        model.addAttribute("items", items);
        model.addAttribute("toppings", toppings);
        model.addAttribute("qrUrl", qrUrl);
        model.addAttribute("addInfo", addInfo);
        model.addAttribute("bankCode", bankCode);
        model.addAttribute("accountNumber", accountNumber);
        model.addAttribute("expiryEpochMillis", expiryEpochMillis);
        model.addAttribute("expiryMinutes", EXPIRY_MINUTES);
        if (accountName != null && !accountName.isBlank()) model.addAttribute("accountName", accountName);
        model.addAttribute("pageTitle", "Thanh toán đơn #" + id);
        return "payment/transfer";
    }

    @GetMapping("/{id}/status")
    @ResponseBody
    public Map<String, Object> getStatus(@PathVariable Integer id,
            @AuthenticationPrincipal CustomerUserDetails principal) {
        Map<String, Object> m = new HashMap<>();
        var opt = orderRepo.findById(id);
        if (opt.isPresent()) {
            Order order = opt.get();
            if (isTransferMethod(order.getPayment().getMethod())
                    && order.getPayment().getStatus() != PaymentStatus.PAID) {
                LocalDateTime expiry = (order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now())
                        .plusMinutes(EXPIRY_MINUTES);
                OrderContext context = new OrderContext(order);
                if (LocalDateTime.now().isAfter(expiry) && context.canCancel()) {
                    context.cancel();
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
        boolean isAdmin = auth != null && auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
        if (!isAdmin) return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();
        order.getPayment().setStatus(PaymentStatus.PAID);
        orderRepo.save(order);
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @GetMapping("/{id}/success")
    public String success(@PathVariable Integer id,
            @AuthenticationPrincipal CustomerUserDetails principal,
            Model model) {
        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) return "redirect:/account/orders";
        if (principal == null || order.getCustomer() == null
                || !java.util.Objects.equals(order.getCustomer().getId(), principal.getId())) {
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
        if (principal == null || order.getCustomer() == null
                || !java.util.Objects.equals(order.getCustomer().getId(), principal.getId())) {
            return "redirect:/account/orders";
        }
        if (order.getPayment().getStatus() == PaymentStatus.PAID) return "redirect:/payment/" + id + "/success";
        OrderContext context = new OrderContext(order);
        if (context.canCancel()) {
            context.cancel();
            orderRepo.save(order);
            ra.addFlashAttribute("msg", "Đã hủy đơn #" + id);
        } else {
            ra.addFlashAttribute("error", "Chỉ hủy được đơn đang chờ xử lý.");
        }
        return "redirect:/account/orders";
    }

    @RequestMapping(value = "/return", method = {RequestMethod.GET, RequestMethod.POST})
    public Object handlePaymentReturn(@RequestParam(required = false) Integer orderId,
                                      @RequestBody(required = false) Map<String, Object> payload,
                                      @RequestHeader(value = "X-Sepay-Signature", required = false) String signature,
                                      jakarta.servlet.http.HttpServletRequest request,
                                      RedirectAttributes ra) {
        
        boolean isPost = "POST".equalsIgnoreCase(request.getMethod());
        System.out.println("[Payment] User/Webhook reached /return. Method: " + request.getMethod() + ", Payload: " + payload);
        
        Integer targetOrderId = orderId;
        
        // 1. Trích xuất Order ID từ payload nếu cần (Hỗ trợ định dạng webhook của SePay)
        if (targetOrderId == null && payload != null) {
            // Thử lấy trực tiếp từ field orderId
            Object pid = payload.get("orderId");
            if (pid != null) {
                try {
                    targetOrderId = Integer.parseInt(pid.toString());
                } catch (Exception e) {}
            }
            
            // Nếu vẫn chưa có, thử trích xuất từ nội dung giao dịch (content)
            if (targetOrderId == null && payload.containsKey("content")) {
                String content = (String) payload.get("content");
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("DH(\\d+)").matcher(content);
                if (matcher.find()) {
                    targetOrderId = Integer.parseInt(matcher.group(1));
                }
            }
        }

        // 2. Nếu là POST (Webhook), thực hiện xử lý thanh toán và trả về 200 OK
        if (isPost && payload != null) {
            try {
                com.alotra.dto.SepayCallbackDTO dto = new com.alotra.dto.SepayCallbackDTO();
                dto.setRaw(payload);
                dto.setOrderId(targetOrderId != null ? targetOrderId.toString() : null);
                // SePay dùng 'transferAmount' cho số tiền nhận được
                dto.setAmount(payload.get("transferAmount") != null ? ((Number) payload.get("transferAmount")).longValue() : 0L);
                dto.setTransactionId(payload.get("id") != null ? String.valueOf(payload.get("id")) : null);
                dto.setStatus("success"); // Giả định là thành công nếu đã gửi đến đây
                dto.setSignature(signature);
                
                // Cập nhật trạng thái thanh toán vào DB (đã được fix trong SepayPaymentProcessor)
                sepayPaymentProcessor.handleSepayCallback(dto);
                
                return ResponseEntity.ok(Map.of("status", "OK", "message", "Processed"));
            } catch (Exception e) {
                System.err.println("[Payment] Error processing fallback webhook: " + e.getMessage());
                return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
            }
        }

        // 3. Nếu là GET (Người dùng quay lại trình duyệt), chuyển hướng về trang lịch sử đơn hàng
        if (targetOrderId != null) {
            ra.addFlashAttribute("message", "Thanh toán cho đơn hàng #" + targetOrderId + " đang được hệ thống xác nhận.");
        }
        
        return "redirect:/account/orders";
    }

    private String buildVietQrUrl(String bankCode, String accountNumber, int amount, String addInfo) {
        String info = URLEncoder.encode(addInfo, StandardCharsets.UTF_8);
        return "https://img.vietqr.io/image/" + bankCode + "-" + accountNumber + "-print.png?amount=" + amount
                + "&addInfo=" + info;
    }
}
