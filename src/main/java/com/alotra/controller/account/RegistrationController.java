package com.alotra.controller.account;

import com.alotra.entity.Customer;
import com.alotra.entity.enums.CustomerStatus;
import com.alotra.repository.CustomerRepository;
import com.alotra.service.OtpService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.Serializable;
import java.time.LocalDateTime;

@Controller
public class RegistrationController {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder encoder;
    private final OtpService otpService;

    public RegistrationController(CustomerRepository customerRepository, PasswordEncoder encoder, OtpService otpService) {
        this.customerRepository = customerRepository;
        this.encoder = encoder;
        this.otpService = otpService;
    }

    public static class RegisterForm implements Serializable {
        @NotBlank private String username;
        @NotBlank private String fullName;
        @Email @NotBlank private String email;
        @NotBlank private String phone;
        @NotBlank private String password;
        @NotBlank private String confirmPassword;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }

    public static class PendingReg implements Serializable {
        private String username;
        private String fullName;
        private String email;
        private String phone;
        private String passwordHash;
        private String otp;
        private LocalDateTime expiresAt;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getPasswordHash() { return passwordHash; }
        public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    }

    @GetMapping("/register")
    public String showRegister(Model model, HttpSession session) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        session.removeAttribute("pendingReg");
        model.addAttribute("pageTitle", "Đăng ký");
        return "auth/register";
    }

    @PostMapping("/register")
    public String startRegister(@ModelAttribute("registerForm") RegisterForm form,
                                BindingResult br,
                                HttpSession session,
                                RedirectAttributes ra) {
        if (form.getPassword() == null || !form.getPassword().equals(form.getConfirmPassword())) {
            ra.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
            ra.addFlashAttribute("registerForm", form);
            return "redirect:/register";
        }
        if (customerRepository.findByUsername(form.getUsername()) != null) {
            ra.addFlashAttribute("error", "Tên đăng nhập đã tồn tại");
            ra.addFlashAttribute("registerForm", form);
            return "redirect:/register";
        }
        if (customerRepository.findByEmail(form.getEmail()) != null) {
            ra.addFlashAttribute("error", "Email đã được sử dụng");
            ra.addFlashAttribute("registerForm", form);
            return "redirect:/register";
        }
        if (form.getPhone() != null && !form.getPhone().isBlank() && customerRepository.findByPhone(form.getPhone()) != null) {
            ra.addFlashAttribute("error", "Số điện thoại đã được sử dụng");
            ra.addFlashAttribute("registerForm", form);
            return "redirect:/register";
        }
        
        PendingReg pr = new PendingReg();
        pr.setUsername(form.getUsername().trim());
        pr.setFullName(form.getFullName().trim());
        pr.setEmail(form.getEmail().trim());
        pr.setPhone(form.getPhone() != null ? form.getPhone().trim() : null);
        pr.setPasswordHash(encoder.encode(form.getPassword()));
        pr.setOtp(otpService.generateOtp());
        pr.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        session.setAttribute("pendingReg", pr);
        otpService.sendOtpEmail(pr.getEmail(), pr.getOtp());
        return "redirect:/register/verify";
    }

    @GetMapping("/register/verify")
    public String showVerify(HttpSession session, Model model, RedirectAttributes ra) {
        PendingReg pr = (PendingReg) session.getAttribute("pendingReg");
        if (pr == null) {
            ra.addFlashAttribute("error", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
            return "redirect:/register";
        }
        model.addAttribute("email", maskEmail(pr.getEmail()));
        model.addAttribute("pageTitle", "Xác nhận OTP");
        return "auth/register-verify";
    }

    @PostMapping("/register/verify")
    public String verify(@RequestParam String otp,
                         HttpSession session,
                         RedirectAttributes ra) {
        PendingReg pr = (PendingReg) session.getAttribute("pendingReg");
        if (pr == null) {
            ra.addFlashAttribute("error", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
            return "redirect:/register";
        }
        if (pr.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.removeAttribute("pendingReg");
            ra.addFlashAttribute("error", "OTP đã hết hạn. Vui lòng đăng ký lại.");
            return "redirect:/register";
        }
        if (!pr.getOtp().equals(otp != null ? otp.trim() : "")) {
            ra.addFlashAttribute("error", "Mã OTP không đúng");
            return "redirect:/register/verify";
        }
        if (customerRepository.findByUsername(pr.getUsername()) != null || customerRepository.findByEmail(pr.getEmail()) != null) {
            session.removeAttribute("pendingReg");
            ra.addFlashAttribute("error", "Tài khoản/Email đã tồn tại. Vui lòng đăng ký lại bằng thông tin khác.");
            return "redirect:/register";
        }
        
        Customer customer = new Customer();
        customer.setUsername(pr.getUsername());
        customer.setFullName(pr.getFullName());
        customer.setEmail(pr.getEmail());
        customer.setPhone(pr.getPhone());
        customer.setPasswordHash(pr.getPasswordHash());
        customer.setStatus(CustomerStatus.ACTIVE);
        customerRepository.save(customer);
        session.removeAttribute("pendingReg");
        return "redirect:/login?activated";
    }

    @PostMapping("/register/resend-otp")
    public String resendOtp(HttpSession session, RedirectAttributes ra) {
        PendingReg pr = (PendingReg) session.getAttribute("pendingReg");
        if (pr == null) {
            ra.addFlashAttribute("error", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
            return "redirect:/register";
        }
        pr.setOtp(otpService.generateOtp());
        pr.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        session.setAttribute("pendingReg", pr);
        otpService.sendOtpEmail(pr.getEmail(), pr.getOtp());
        ra.addFlashAttribute("message", "Đã gửi lại OTP");
        return "redirect:/register/verify";
    }

    private String maskEmail(String email) {
        if (email == null) return "";
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(Math.max(0, at));
        String name = email.substring(0, at);
        String domain = email.substring(at);
        String masked = name.charAt(0) + "***" + name.charAt(name.length() - 1);
        return masked + domain;
    }
}