package com.alotra.controller.admin;

import com.alotra.entity.Review;
import com.alotra.entity.Employee;
import com.alotra.repository.ReviewRepository;
import com.alotra.repository.EmployeeRepository;
import com.alotra.service.infrastructure.EmailService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {
    private final ReviewRepository reviewRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;

    public AdminReviewController(ReviewRepository reviewRepository, EmployeeRepository employeeRepository, EmailService emailService) {
        this.reviewRepository = reviewRepository;
        this.employeeRepository = employeeRepository;
        this.emailService = emailService;
    }

    @GetMapping
    public String list(Model model) {
        List<Review> items = reviewRepository.findAll();
        model.addAttribute("items", items);
        model.addAttribute("pageTitle", "Đánh giá sản phẩm");
        model.addAttribute("currentPage", "reviews");
        return "admin/reviews";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        if (id != null) {
            reviewRepository.findById(id).ifPresent(reviewRepository::delete);
        }
        ra.addFlashAttribute("message", "Đã xóa đánh giá #" + id);
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/reply")
    public String reply(@PathVariable Integer id,
                        @RequestParam("reply") String reply,
                        Authentication auth,
                        RedirectAttributes ra) {
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null) {
            ra.addFlashAttribute("error", "Không tìm thấy đánh giá.");
            return "redirect:/admin/reviews";
        }
        String who = auth != null ? auth.getName() : "admin";
        String content = (reply != null && !reply.isBlank()) ? reply.trim() : null;
        review.setAdminReply(content);
        Employee replier = employeeRepository.findByUsername(who).orElse(null);
        review.setRepliedBy(content != null ? replier : null);
        reviewRepository.save(review);

        if (content != null) {
            try {
                String to = review.getCustomer() != null ? review.getCustomer().getEmail() : null;
                if (to != null && !to.isBlank()) {
                    String subject = "AloTra - Cửa hàng đã phản hồi đánh giá của bạn";
                    String body = "Xin chào,\n\n" +
                            "Cửa hàng đã phản hồi đánh giá của bạn:\n\n" +
                            content + "\n\n" +
                            "Bạn có thể xem chi tiết trong mục Đơn hàng của tôi.";
                    emailService.send(to, subject, body);
                }
            } catch (Exception ignored) { }
        }
        ra.addFlashAttribute("message", content == null ? "Đã xóa phản hồi." : "Đã gửi phản hồi.");
        return "redirect:/admin/reviews";
    }
}
