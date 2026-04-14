package com.alotra.controller.account;

import com.alotra.entity.Customer;
import com.alotra.service.account.AccountFacade;
import com.alotra.service.account.AddressService;
import com.alotra.service.order.OrderHistoryService;
import com.alotra.service.query.CustomerOrderHistoryQuery;
import com.alotra.repository.OrderRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequestMapping("/account")
public class AccountController {
    private final AccountFacade accountFacade;
    private final AddressService addressService;
    private final OrderHistoryService orderHistoryService;
    private final OrderRepository orderRepository;

    public AccountController(AccountFacade accountFacade, 
                             AddressService addressService,
                             OrderHistoryService orderHistoryService,
                             OrderRepository orderRepository) {
        this.accountFacade = accountFacade;
        this.addressService = addressService;
        this.orderHistoryService = orderHistoryService;
        this.orderRepository = orderRepository;
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
