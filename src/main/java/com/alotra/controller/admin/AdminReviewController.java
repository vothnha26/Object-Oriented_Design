package com.alotra.controller.admin;

import com.alotra.entity.Employee;
import com.alotra.entity.Review;
import com.alotra.repository.CustomerRepository;
import com.alotra.repository.ProductRepository;
import com.alotra.repository.ReviewRepository;
import com.alotra.util.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public String list(Model model) {
        List<Review> reviews = reviewRepository.findAll();
        model.addAttribute("reviews", reviews);
        model.addAttribute("customerRepository", customerRepository);
        model.addAttribute("productRepository", productRepository);
        return "admin/reviews/list";
    }

    @PostMapping("/{id}/reply")
    public String reply(@PathVariable Integer id, @RequestParam String reply, HttpSession session) {
        Employee admin = SessionUtils.getEmployee(session);
        Review review = reviewRepository.findById(id).orElseThrow();
        review.setAdminReply(reply);
        // review.setRepliedBy(admin); // Bỏ trường này nếu không có trong PUML
        reviewRepository.save(review);
        return "redirect:/admin/reviews";
    }
}
