package com.alotra.auth;

import com.alotra.entity.Customer;
import com.alotra.entity.Employee;
import com.alotra.repository.CustomerRepository;
import com.alotra.repository.EmployeeRepository;
import com.alotra.service.infrastructure.EmailService;
import com.alotra.service.account.PasswordResetTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping
public class PasswordResetController {
    private final CustomerRepository customerRepo;
    private final EmployeeRepository employeeRepo;
    private final PasswordResetTokenService tokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(CustomerRepository customerRepo,
                                   EmployeeRepository employeeRepo,
                                   PasswordResetTokenService tokenService,
                                   EmailService emailService,
                                   PasswordEncoder passwordEncoder) {
        this.customerRepo = customerRepo;
        this.employeeRepo = employeeRepo;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/forgot-password")
    public String forgotForm(Model model) {
        model.addAttribute("pageTitle", "Quên mật khẩu");
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgot(@RequestParam("email") String email,
                               RedirectAttributes ra) {
        String e = (email != null ? email.trim() : "");
        // Generate token regardless to avoid leaking existence
        if (StringUtils.hasText(e)) {
            try {
                String token = tokenService.generateToken(e);
                String resetUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/reset-password")
                        .queryParam("token", token)
                        .build().toUriString();
                String subject = "Đặt lại mật khẩu AloTra";
                String body = "Xin chào,\n\n" +
                        "Bạn (hoặc ai đó) đã yêu cầu đặt lại mật khẩu cho tài khoản tại AloTra.\n" +
                        "Nhấp vào liên kết sau để đặt lại mật khẩu (hiệu lực trong 15 phút):\n" +
                        resetUrl + "\n\n" +
                        "Nếu bạn không yêu cầu, hãy bỏ qua email này.";
                // Send only if email exists to avoid backscatter? We still send to provided address.
                // Safer: only send when account exists
                boolean exists = customerRepo.findByEmail(e).isPresent() || employeeRepo.findByEmail(e).isPresent();
                if (exists) {
                    emailService.send(e, subject, body);
                }
            } catch (Exception ignore) {
                // Do not leak details
            }
        }
        ra.addFlashAttribute("message", "Nếu email hợp lệ, chúng tôi đã gửi hướng dẫn đặt lại mật khẩu.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetForm(@RequestParam(value = "token", required = false) String token,
                            Model model, RedirectAttributes ra) {
        String email = tokenService.validateAndExtractEmail(token);
        if (email == null) {
            ra.addFlashAttribute("error", "Liên kết đặt lại không hợp lệ hoặc đã hết hạn.");
            return "redirect:/forgot-password";
        }
        model.addAttribute("pageTitle", "Đặt lại mật khẩu");
        model.addAttribute("token", token);
        model.addAttribute("emailMasked", maskEmail(email));
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleReset(@RequestParam("token") String token,
                              @RequestParam("password") String password,
                              @RequestParam("confirmPassword") String confirm,
                              RedirectAttributes ra) {
        String email = tokenService.validateAndExtractEmail(token);
        if (email == null) {
            ra.addFlashAttribute("error", "Liên kết đặt lại không hợp lệ hoặc đã hết hạn.");
            return "redirect:/forgot-password";
        }
        if (!StringUtils.hasText(password) || password.length() < 6) {
            ra.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
            return "redirect:/reset-password?token=" + token;
        }
        if (!password.equals(confirm)) {
            ra.addFlashAttribute("error", "Xác nhận mật khẩu không khớp.");
            return "redirect:/reset-password?token=" + token;
        }
        // Update for customer or employee
        boolean updated = false;
        Customer customer = customerRepo.findByEmail(email).orElse(null);
        if (customer != null) {
            customer.setPasswordHash(passwordEncoder.encode(password));
            customerRepo.save(customer);
            updated = true;
        } else {
            Employee employee = employeeRepo.findByEmail(email).orElse(null);
            if (employee != null) {
                employee.setPasswordHash(passwordEncoder.encode(password));
                employeeRepo.save(employee);
                updated = true;
            }
        }
        if (updated) {
            ra.addFlashAttribute("msg", "Đã cập nhật mật khẩu. Bạn có thể đăng nhập.");
            return "redirect:/login";
        }
        // Email no longer exists
        ra.addFlashAttribute("error", "Không thể cập nhật mật khẩu cho email này.");
        return "redirect:/forgot-password";
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return "***";
        String name = email.substring(0, at);
        String domain = email.substring(at);
        String masked = name.charAt(0) + "***" + name.charAt(name.length()-1);
        return masked + domain;
    }
}
