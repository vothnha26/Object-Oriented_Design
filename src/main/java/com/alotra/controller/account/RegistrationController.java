package com.alotra.controller.account;

import com.alotra.dto.TempRegistrationDTO;
import com.alotra.service.account.AccountFacade;
import com.alotra.util.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Random;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final AccountFacade accountFacade;

    public RegistrationController(AccountFacade accountFacade) {
        this.accountFacade = accountFacade;
    }

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("pageTitle", "Đăng ký tài khoản");
        return "auth/register";
    }

    @PostMapping
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String fullName,
                           @RequestParam String phone,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           HttpSession session,
                           RedirectAttributes ra) {
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
            return "redirect:/register";
        }
        
        try {
            // Tạo DTO gom nhóm dữ liệu
            TempRegistrationDTO tempReg = new TempRegistrationDTO();
            tempReg.setUsername(username);
            tempReg.setEmail(email);
            tempReg.setFullName(fullName);
            tempReg.setPhone(phone);
            tempReg.setPassword(password);
            tempReg.setOtp(String.format("%06d", new Random().nextInt(999999)));

            // Sử dụng SessionKeys class
            session.setAttribute(SessionKeys.getRegistrationData(), tempReg);
            session.setMaxInactiveInterval(300); // 5 phút

            System.out.println(">>> [EMAIL SERVICE] Gửi mã OTP đến " + email + ": " + tempReg.getOtp());
            
            ra.addFlashAttribute("email", email);
            return "redirect:/register/verify";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/verify")
    public String showVerifyForm(HttpSession session, Model model) {
        TempRegistrationDTO tempReg = (TempRegistrationDTO) session.getAttribute(SessionKeys.getRegistrationData());
        if (tempReg == null) return "redirect:/register";
        
        model.addAttribute("pageTitle", "Xác thực tài khoản");
        model.addAttribute("email", tempReg.getEmail());
        return "auth/register-verify";
    }

    @PostMapping("/verify")
    public String verifyOtp(@RequestParam String otp, HttpSession session, RedirectAttributes ra) {
        String key = SessionKeys.getRegistrationData();
        TempRegistrationDTO tempReg = (TempRegistrationDTO) session.getAttribute(key);

        if (tempReg != null && tempReg.getOtp().equals(otp)) {
            accountFacade.registerCustomer(
                tempReg.getUsername(), 
                tempReg.getEmail(), 
                tempReg.getFullName(), 
                tempReg.getPhone(), 
                tempReg.getPassword()
            );
            
            session.removeAttribute(key);
            ra.addFlashAttribute("message", "Xác thực thành công! Bạn có thể đăng nhập ngay.");
            return "redirect:/login";
        } else {
            ra.addFlashAttribute("error", "Mã OTP không chính xác hoặc đã hết hạn");
            return "redirect:/register/verify";
        }
    }
}
