package com.alotra.controller.account;

import com.alotra.entity.Customer;
import com.alotra.entity.Review;
import com.alotra.entity.enums.OrderStatus;
import com.alotra.entity.enums.PaymentStatus;
import com.alotra.security.CustomerUserDetails;
import com.alotra.service.OrderHistoryService;
import com.alotra.service.CustomerService;
import com.alotra.service.proxy.ReviewOperations;
import com.alotra.service.ReviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final OrderHistoryService orderService;
    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;
    private final ReviewOperations reviewService;

    public AccountController(OrderHistoryService orderService,
                             CustomerService customerService,
                             PasswordEncoder passwordEncoder,
                             ReviewOperations reviewService) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.passwordEncoder = passwordEncoder;
        this.reviewService = reviewService;
    }

    @GetMapping("/profile")
    public String showProfilePage(@AuthenticationPrincipal CustomerUserDetails current,
                                  Model model,
                                  RedirectAttributes ra) {
        if (current == null || current.getId() == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập để xem trang này.");
            return "redirect:/login";
        }
        
        model.addAttribute("pageTitle", "Thông Tin Tài Khoản");
        Customer customer = customerService.findById(current.getId());
        
        if (customer == null) {
            ra.addFlashAttribute("error", "Không tìm thấy thông tin tài khoản.");
            return "redirect:/";
        }
        
        ProfileForm form = new ProfileForm();
        form.fullName = customer.getFullName();
        form.email = customer.getEmail();
        form.phone = customer.getPhone();
        model.addAttribute("form", form);
        model.addAttribute("kh", customer);
        return "account/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomerUserDetails current,
                                @ModelAttribute("form") ProfileForm form,
                                BindingResult result,
                                RedirectAttributes ra,
                                Model model) {
        if (current == null || current.getId() == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập để thực hiện thao tác này.");
            return "redirect:/login";
        }
        
        Customer customer = customerService.findById(current.getId());
        if (customer == null) {
            ra.addFlashAttribute("error", "Không tìm thấy thông tin tài khoản.");
            return "redirect:/";
        }
        
        if (form.email != null && !form.email.equalsIgnoreCase(customer.getEmail())) {
            Customer byEmail = customerService.findByEmail(form.email);
            if (byEmail != null && !byEmail.getId().equals(customer.getId())) {
                result.rejectValue("email", "dup", "Email đã được sử dụng.");
            }
        }
        if (form.phone != null && !form.phone.isBlank()) {
            Customer byPhone = customerService.findByPhone(form.phone);
            if (byPhone != null && !byPhone.getId().equals(customer.getId())) {
                result.rejectValue("phone", "dup", "Số điện thoại đã được sử dụng.");
            }
        }
        boolean wantChangePwd = form.newPassword != null && !form.newPassword.isBlank();
        if (wantChangePwd) {
            if (form.confirmPassword == null || !form.newPassword.equals(form.confirmPassword)) {
                result.rejectValue("confirmPassword", "mismatch", "Mật khẩu xác nhận không khớp.");
            }
        }
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Thông Tin Tài Khoản");
            model.addAttribute("kh", customer);
            return "account/profile";
        }
        customer.setFullName(form.fullName);
        customer.setEmail(form.email);
        customer.setPhone(form.phone);
        if (wantChangePwd) {
            customer.setPasswordHash(passwordEncoder.encode(form.newPassword));
        }
        customerService.save(customer);
        ra.addFlashAttribute("message", "Cập nhật thông tin thành công.");
        return "redirect:/account/profile";
    }

    @GetMapping("/orders")
    public String showOrdersPage(@AuthenticationPrincipal CustomerUserDetails current,
                                 @RequestParam(value = "status", required = false) String status,
                                 @RequestParam(value = "code", required = false) String code,
                                 @RequestParam(value = "from", required = false)
                                 @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
                                 @RequestParam(value = "to", required = false)
                                 @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
                                 RedirectAttributes ra,
                                 Model model) {
        if (current == null || current.getId() == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập để xem trang này.");
            return "redirect:/login";
        }
        
        model.addAttribute("pageTitle", "Lịch Sử Đơn Hàng");
        Integer orderId = null;
        if (code != null && !code.isBlank()) {
            try { orderId = Integer.valueOf(code.trim()); } catch (NumberFormatException ignored) {}
        }
        LocalDateTime fromDt = null, toDt = null;
        if (from != null || to != null) {
            fromDt = (from != null) ? from.atStartOfDay() : null;
            toDt = (to != null) ? to.atTime(23,59,59) : LocalDateTime.now();
            if (fromDt != null && toDt != null && toDt.isBefore(fromDt)) {
                LocalDateTime tmp = fromDt; fromDt = toDt; toDt = tmp;
            }
        }
        List<OrderHistoryService.OrderRow> list = orderService.listOrdersByCustomer(current.getId(), status, orderId, fromDt, toDt);
        model.addAttribute("items", list);
        model.addAttribute("status", status);
        model.addAttribute("code", code);
        model.addAttribute("from", from != null ? from.toString() : "");
        model.addAttribute("to", to != null ? to.toString() : "");
        return "account/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable("id") Integer id,
                              @AuthenticationPrincipal CustomerUserDetails current,
                              RedirectAttributes ra,
                              Model model) {
        if (current == null || current.getId() == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập để xem trang này.");
            return "redirect:/login";
        }
        
        var order = orderService.getOrderOfCustomer(id, current.getId());
        if (order == null) {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return "redirect:/account/orders";
        }
        var items = orderService.listOrderItems(id);
        Map<Integer, List<OrderHistoryService.ItemToppingRow>> toppings = new HashMap<>();
        for (var it : items) {
            toppings.put(it.id, orderService.listOrderedToppings(it.id));
        }
        List<Integer> lineIds = items.stream().map(it -> it.id).collect(Collectors.toList());
        Map<Integer, Review> reviewsByLine = reviewService.findExistingByCustomerAndLines(current.getId(), lineIds);
        boolean eligibleForReview = reviewService.isOrderEligibleForReview(
            order.status != null ? OrderStatus.valueOf(order.status) : null,
            order.paymentStatus != null ? PaymentStatus.valueOf(order.paymentStatus) : null
        );
        Map<Integer, Boolean> reviewEditableByLine = new HashMap<>();
        reviewsByLine.forEach((lineId, rv) -> reviewEditableByLine.put(lineId, reviewService.canEdit(rv)));
        
        model.addAttribute("pageTitle", "Chi tiết đơn #" + id);
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("toppings", toppings);
        model.addAttribute("reviewsByLine", reviewsByLine);
        model.addAttribute("eligibleForReview", eligibleForReview);
        model.addAttribute("reviewEditableByLine", reviewEditableByLine);
        model.addAttribute("editWindowMinutes", ReviewService.EDIT_WINDOW.toMinutes());
        return "account/order-detail";
    }

    public static class ProfileForm {
        @NotBlank
        public String fullName;
        @NotBlank
        @Email
        public String email;
        public String phone;
        public String newPassword;
        public String confirmPassword;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }
}