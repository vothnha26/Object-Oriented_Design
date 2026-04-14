package com.alotra.controller.account;

import com.alotra.entity.Customer;
import com.alotra.service.account.AccountFacade;
import com.alotra.service.account.AddressService;
import com.alotra.service.order.OrderHistoryService;
import com.alotra.service.query.CustomerOrderHistoryQuery;
import com.alotra.repository.OrderRepository;
import com.alotra.repository.ReviewRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/account")
public class AccountController {
    private final AccountFacade accountFacade;
    private final AddressService addressService;
    private final OrderHistoryService orderHistoryService;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final com.alotra.service.proxy.ReviewOperations reviewOperations;

    public AccountController(AccountFacade accountFacade, 
                             AddressService addressService,
                             OrderHistoryService orderHistoryService,
                             OrderRepository orderRepository,
                             ReviewRepository reviewRepository,
                             com.alotra.service.proxy.ReviewOperations reviewOperations) {
        this.accountFacade = accountFacade;
        this.addressService = addressService;
        this.orderHistoryService = orderHistoryService;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.reviewOperations = reviewOperations;
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Integer id, Authentication auth, Model model) {
        Customer customer = accountFacade.findByUsername(auth.getName());
        com.alotra.entity.Order order = orderRepository.findById(id).orElse(null);
        
        if (order == null || !java.util.Objects.equals(order.getCustomer().getId(), customer.getId())) {
            return "redirect:/account/orders";
        }

        var items = orderHistoryService.listOrderItems(id);
        Map<Integer, List<OrderHistoryService.ItemToppingRow>> toppings = new HashMap<>();
        for (var it : items) {
            toppings.put(it.id, orderHistoryService.listOrderedToppings(it.id));
        }

        // Đánh giá
        List<com.alotra.entity.Review> reviews = reviewRepository.findByProductId(null); // Just to get the type right for now, will filter below
        Map<Integer, com.alotra.entity.Review> reviewsByLine = new HashMap<>();
        Map<Integer, Boolean> reviewEditableByLine = new HashMap<>();
        
        // Lấy tất cả review của đơn hàng này
        List<com.alotra.entity.Review> orderReviews = reviewRepository.findAll().stream()
                .filter(r -> r.getOrder() != null && r.getOrder().getId().equals(id))
                .toList();

        for (var it : items) {
            orderReviews.stream()
                .filter(r -> r.getProduct().getName().equals(it.productName))
                .findFirst()
                .ifPresent(r -> {
                    reviewsByLine.put(it.id, r);
                    reviewEditableByLine.put(it.id, reviewOperations.canEdit(r));
                });
        }

        boolean eligible = false;
        if (order.getPayment() != null) {
            eligible = reviewOperations.isOrderEligibleForReview(order.getStatus(), order.getPayment().getStatus());
        }

        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("toppings", toppings);
        model.addAttribute("reviewsByLine", reviewsByLine);
        model.addAttribute("reviewEditableByLine", reviewEditableByLine);
        model.addAttribute("eligibleForReview", eligible);
        model.addAttribute("editWindowMinutes", 15);
        model.addAttribute("pageTitle", "Chi tiết đơn #" + id);
        
        return "account/order-detail";
    }

    @PostMapping("/orders/{id}/review")
    public String submitOrderReview(@PathVariable Integer id,
                                    @RequestParam Integer lineId,
                                    @RequestParam int stars,
                                    @RequestParam(required = false) String comment,
                                    Authentication auth, RedirectAttributes ra) {
        Customer customer = accountFacade.findByUsername(auth.getName());
        try {
            // Lấy productId từ OrderItem
            com.alotra.entity.OrderItem item = orderRepository.findById(id).get().getItems().stream()
                    .filter(i -> i.getId().equals(lineId))
                    .findFirst().orElseThrow();
            
            reviewOperations.submitReview(customer, item.getVariant().getProduct().getId(), id, stars, comment);
            ra.addFlashAttribute("message", "Cảm ơn bạn đã đánh giá!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/account/orders/" + id;
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        Customer customer = accountFacade.findByUsername(auth.getName());
        
        com.alotra.dto.ProfileForm form = new com.alotra.dto.ProfileForm();
        form.setFullName(customer.getFullName());
        form.setEmail(customer.getEmail());
        form.setPhone(customer.getPhone());
        
        model.addAttribute("customer", customer);
        model.addAttribute("form", form);
        model.addAttribute("addresses", addressService.findByCustomer(customer.getId()));
        model.addAttribute("pageTitle", "Thông tin tài khoản");
        return "account/profile";
    }

    @PostMapping("/addresses/add")
    public String addAddress(Authentication auth, 
                             @RequestParam String label,
                             @RequestParam String addressLine,
                             @RequestParam(required = false) boolean isDefault,
                             RedirectAttributes ra) {
        try {
            Customer customer = accountFacade.findByUsername(auth.getName());
            com.alotra.entity.Address addr = new com.alotra.entity.Address();
            addr.setLabel(label);
            addr.setAddressLine(addressLine);
            addr.setDefault(isDefault);
            addressService.save(addr, customer);
            ra.addFlashAttribute("message", "Thêm địa chỉ thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/account/profile";
    }

    @PostMapping("/addresses/delete/{id}")
    public String deleteAddress(Authentication auth, @PathVariable Integer id, RedirectAttributes ra) {
        addressService.delete(id);
        ra.addFlashAttribute("message", "Đã xóa địa chỉ");
        return "redirect:/account/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Authentication auth, 
                                @ModelAttribute("form") com.alotra.dto.ProfileForm form, 
                                RedirectAttributes ra) {
        try {
            accountFacade.updateProfile(auth.getName(), form.getFullName(), form.getEmail(), form.getPhone());
            
            if (form.getNewPassword() != null && !form.getNewPassword().isBlank()) {
                if (!form.getNewPassword().equals(form.getConfirmPassword())) {
                    ra.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
                    return "redirect:/account/profile";
                }
                accountFacade.changePassword(auth.getName(), form.getNewPassword());
            }
            
            ra.addFlashAttribute("message", "Cập nhật thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/account/profile";
    }

    @GetMapping("/orders")
    public String orders(Authentication auth, 
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String code,
                         @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate from,
                         @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate to,
                         Model model) {
        Customer customer = accountFacade.findByUsername(auth.getName());
        
        // Sử dụng Template Method thông qua CustomerOrderHistoryQuery
        CustomerOrderHistoryQuery query = new CustomerOrderHistoryQuery(orderRepository, customer.getId());
        
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt = to != null ? to.atTime(LocalTime.MAX) : null;

        model.addAttribute("items", query.execute(code, status, fromDt, toDt, null));
        model.addAttribute("status", status);
        model.addAttribute("code", code);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("pageTitle", "Lịch sử đơn hàng");
        return "account/orders";
    }

    @GetMapping("/addresses")
    public String addresses(Authentication auth, Model model) {
        Customer customer = accountFacade.findByUsername(auth.getName());
        model.addAttribute("addresses", addressService.findByCustomer(customer.getId()));
        return "account/profile";
    }
}
