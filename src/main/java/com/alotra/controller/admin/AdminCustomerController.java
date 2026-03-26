package com.alotra.controller.admin;

import com.alotra.entity.Customer;
import com.alotra.entity.enums.CustomerStatus;
import com.alotra.service.CustomerService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {
    private final CustomerService customerService;

    public AdminCustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String list(Model model) {
        List<Customer> items = customerService.findAll();
        model.addAttribute("pageTitle", "Khách hàng");
        model.addAttribute("currentPage", "customers");
        model.addAttribute("items", items);
        return "admin/customers";
    }

    @PostMapping("/{id}/lock")
    public String lock(@PathVariable Integer id, RedirectAttributes ra) {
        Customer customer = customerService.findById(id);
        if (customer == null) {
            ra.addFlashAttribute("error", "Không tìm thấy khách hàng.");
        } else {
            customer.setStatus(CustomerStatus.INACTIVE);
            customerService.save(customer);
            ra.addFlashAttribute("message", "Đã khóa tài khoản khách hàng.");
        }
        return "redirect:/admin/customers";
    }

    @PostMapping("/{id}/unlock")
    public String unlock(@PathVariable Integer id, RedirectAttributes ra) {
        Customer customer = customerService.findById(id);
        if (customer == null) {
            ra.addFlashAttribute("error", "Không tìm thấy khách hàng.");
        } else {
            customer.setStatus(CustomerStatus.ACTIVE);
            customerService.save(customer);
            ra.addFlashAttribute("message", "Đã mở khóa tài khoản khách hàng.");
        }
        return "redirect:/admin/customers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Customer customer = customerService.findById(id);
            if (customer == null) {
                ra.addFlashAttribute("error", "Không tìm thấy khách hàng.");
            } else {
                customerService.deleteById(id);
                ra.addFlashAttribute("message", "Đã xóa tài khoản khách hàng.");
            }
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", "Không thể xóa vì tài khoản đã phát sinh dữ liệu (đơn hàng/đánh giá). Hãy khóa thay vì xóa.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Không thể xóa: " + ex.getMessage());
        }
        return "redirect:/admin/customers";
    }
}
