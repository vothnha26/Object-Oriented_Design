package com.alotra.controller.account;

import com.alotra.entity.Address;
import com.alotra.entity.Customer;
import com.alotra.entity.Order;
import com.alotra.repository.OrderRepository;
import com.alotra.service.AddressService;
import com.alotra.service.CustomerService;
import com.alotra.service.ReviewService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {
    private final CustomerService customerService;
    private final AddressService addressService;
    private final OrderRepository orderRepository;
    private final ReviewService reviewService;
    private final PasswordEncoder passwordEncoder;

    public AccountController(CustomerService customerService,
                             AddressService addressService,
                             OrderRepository orderRepository,
                             ReviewService reviewService,
                             PasswordEncoder passwordEncoder) {
        this.customerService = customerService;
        this.addressService = addressService;
        this.orderRepository = orderRepository;
        this.reviewService = reviewService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        Customer customer = customerService.findByUsername(auth.getName());
        model.addAttribute("customer", customer);
        model.addAttribute("pageTitle", "Thông tin tài khoản");
        return "account/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Authentication auth, @ModelAttribute Customer data, RedirectAttributes ra) {
        Customer customer = customerService.findByUsername(auth.getName());
        customer.setFullName(data.getFullName());
        customer.setEmail(data.getEmail());
        customer.setPhone(data.getPhone());
        customerService.save(customer);
        ra.addFlashAttribute("message", "Cập nhật thành công");
        return "redirect:/account/profile";
    }

    @GetMapping("/orders")
    public String orders(Authentication auth, Model model) {
        Customer customer = customerService.findByUsername(auth.getName());
        List<Order> orders = orderRepository.findByCustomerId(customer.getId());
        model.addAttribute("orders", orders);
        model.addAttribute("pageTitle", "Lịch sử đơn hàng");
        return "account/orders";
    }

    @GetMapping("/addresses")
    public String addresses(Authentication auth, Model model) {
        Customer customer = customerService.findByUsername(auth.getName());
        model.addAttribute("addresses", addressService.findByCustomer(customer.getId()));
        return "account/profile"; // Giả sử hiển thị chung trang profile
    }
}
