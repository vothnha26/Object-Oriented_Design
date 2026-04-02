package com.alotra.controller.account;

import com.alotra.entity.Customer;
import com.alotra.service.account.CustomerService;
import com.alotra.service.proxy.ReviewOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account/reviews")
public class AccountReviewController {
    private final ReviewOperations reviewService;
    private final CustomerService customerService;

    public AccountReviewController(ReviewOperations reviewService, CustomerService customerService) {
        this.reviewService = reviewService;
        this.customerService = customerService;
    }

    @PostMapping("/submit")
    public String submit(@RequestParam("productId") Integer productId,
                         @RequestParam("orderId") Integer orderId,
                         @RequestParam("stars") int stars,
                         @RequestParam(value = "comment", required = false) String comment,
                         Authentication auth, RedirectAttributes ra) {
        Customer customer = customerService.findByUsername(auth.getName());
        try {
            reviewService.submitReview(customer, productId, orderId, stars, comment);
            ra.addFlashAttribute("message", "Đã gửi đánh giá");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/account/orders";
    }
}
