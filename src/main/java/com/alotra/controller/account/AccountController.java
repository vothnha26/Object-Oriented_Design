package com.alotra.controller.account;

import com.alotra.entity.Customer;
import com.alotra.entity.Order;
import com.alotra.service.account.AccountFacade;
import com.alotra.service.account.AddressService;
import com.alotra.service.order.OrderFacade;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {
    private final AccountFacade accountFacade;
    private final OrderFacade orderFacade;
    private final AddressService addressService;

    public AccountController(AccountFacade accountFacade, 
                             OrderFacade orderFacade, 
                             AddressService addressService) {
        this.accountFacade = accountFacade;
        this.orderFacade = orderFacade;
        this.addressService = addressService;
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        Customer customer = accountFacade.findByUsername(auth.getName());
        model.addAttribute("customer", customer);
        model.addAttribute("pageTitle", "Thông tin tài khoản");
        return "account/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Authentication auth, @ModelAttribute Customer data, RedirectAttributes ra) {
        accountFacade.updateProfile(auth.getName(), data.getFullName(), data.getEmail(), data.getPhone());
        ra.addFlashAttribute("message", "Cập nhật thành công");
        return "redirect:/account/profile";
    }

    @GetMapping("/orders")
    public String orders(Authentication auth, Model model) {
        Customer customer = accountFacade.findByUsername(auth.getName());
        List<Order> orders = orderFacade.getCustomerOrderHistory(customer.getId());
        model.addAttribute("orders", orders);
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
