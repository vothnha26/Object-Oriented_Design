package com.alotra.controller.account;

import com.alotra.entity.Customer;
import com.alotra.entity.Review;
import com.alotra.repository.ProductRepository;
import com.alotra.service.interaction.ReviewService;
import com.alotra.util.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/reviews")
    public String myReviews(HttpSession session, Model model) {
        Customer customer = SessionUtils.getCustomer(session);
        if (customer == null) return "redirect:/login";

        List<Review> reviews = reviewService.getMyReviews(customer);
        model.addAttribute("reviews", reviews);
        model.addAttribute("productRepository", productRepository); // Để lấy tên SP từ ID
        return "account/reviews";
    }
}
