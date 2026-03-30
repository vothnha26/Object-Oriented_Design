package com.alotra.controller.account;

import com.alotra.entity.Customer;
import com.alotra.security.CustomerUserDetails;
import com.alotra.service.ReviewService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class AccountReviewController {
    private final ReviewService reviewService;

    public AccountReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    private Customer currentCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object p = auth != null ? auth.getPrincipal() : null;
        if (p instanceof CustomerUserDetails kh) return kh.getCustomer();
        return null;
    }

    @PostMapping("/orders/{orderId}/review")
    public String create(@PathVariable Integer orderId,
                         @RequestParam("lineId") Integer lineId,
                         @RequestParam("stars") Integer stars,
                         @RequestParam(value = "comment", required = false) String comment,
                         RedirectAttributes ra) {
        try {
            Customer customer = currentCustomer();
            if (customer == null) throw new IllegalStateException("Bạn cần đăng nhập");
            reviewService.submitReview(customer, lineId, stars != null ? stars : 5, comment);
            ra.addFlashAttribute("msg", "Đã gửi đánh giá.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/account/orders/" + orderId;
    }

    @PostMapping("/reviews/{id}/edit")
    public String edit(@PathVariable Integer id,
                       @RequestParam("orderId") Integer orderId,
                       @RequestParam("stars") Integer stars,
                       @RequestParam(value = "comment", required = false) String comment,
                       RedirectAttributes ra) {
        try {
            Customer customer = currentCustomer();
            if (customer == null) throw new IllegalStateException("Bạn cần đăng nhập");
            reviewService.updateIfAllowed(customer, id, stars, comment);
            ra.addFlashAttribute("msg", "Đã cập nhật đánh giá.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/account/orders/" + orderId;
    }

    @PostMapping("/reviews/{id}/delete")
    public String delete(@PathVariable Integer id,
                         @RequestParam("orderId") Integer orderId,
                         RedirectAttributes ra) {
        try {
            Customer customer = currentCustomer();
            if (customer == null) throw new IllegalStateException("Bạn cần đăng nhập");
            reviewService.deleteIfAllowed(customer, id);
            ra.addFlashAttribute("msg", "Đã xóa đánh giá.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/account/orders/" + orderId;
    }
}
