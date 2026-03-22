package com.alotra.service;

import com.alotra.entity.KhachHang;
import com.alotra.entity.OtpCode;
import com.alotra.repository.KhachHangRepository;
import com.alotra.repository.OtpCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {
    public static final String TYPE_REGISTER = "REGISTER";
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private final OtpCodeRepository otpRepo;
    private final KhachHangRepository khRepo;
    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final SecureRandom random = new SecureRandom();

    public OtpService(OtpCodeRepository otpRepo,
                      KhachHangRepository khRepo,
                      JavaMailSender mailSender,
                      @Value("${app.mail.from:${spring.mail.username}}") String fromAddress) {
        this.otpRepo = otpRepo;
        this.khRepo = khRepo;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public String generateNumericCode(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    @Transactional
    public void sendRegisterOtp(KhachHang kh) {
        // cleanup expired old codes for this user/type
        otpRepo.deleteByCustomerAndTypeAndExpiresAtBefore(kh, TYPE_REGISTER, LocalDateTime.now().minusDays(1));
        String code = generateNumericCode(6);
        OtpCode otp = new OtpCode();
        otp.setCustomer(kh);
        otp.setType(TYPE_REGISTER);
        otp.setCode(code);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepo.save(otp);
        sendMail(kh.getEmail(), "AloTra - Mã xác thực đăng ký", buildRegisterMailBody(kh, code));
    }

    public String buildRegisterMailBody(KhachHang kh, String code) {
        String name = kh.getFullName() != null ? kh.getFullName() : kh.getUsername();
        return "Xin chào " + name + ",\n\n" +
                "Cảm ơn bạn đã đăng ký tài khoản AloTra. Mã xác thực (OTP) của bạn là: " + code + "\n" +
                "Mã sẽ hết hạn sau 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n" +
                "Trân trọng,\nAloTra";
    }

    private void sendMail(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        // Ensure a valid From is always set to avoid: "can't determine local email address"
        if (fromAddress != null && !fromAddress.isBlank()) {
            msg.setFrom(fromAddress.trim());
        }
        try {
            mailSender.send(msg);
        } catch (org.springframework.mail.MailException ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage(), ex);
            throw ex; // let controller decide how to handle (will result in 500 if unhandled)
        }
    }

    @Transactional
    public boolean verifyRegisterOtp(String email, String code, StringBuilder error) {
        if (email == null || code == null || code.isBlank()) {
            if (error != null) error.append("Thiếu email hoặc mã OTP.");
            return false;
        }
        KhachHang kh = khRepo.findByEmail(email);
        if (kh == null) {
            if (error != null) error.append("Không tìm thấy tài khoản theo email.");
            return false;
        }
        Optional<OtpCode> opt = otpRepo.findTopByCustomerAndTypeAndCodeOrderByIdDesc(kh, TYPE_REGISTER, code.trim());
        if (opt.isEmpty()) {
            if (error != null) error.append("Mã OTP không đúng.");
            return false;
        }
        OtpCode otp = opt.get();
        if (otp.getUsedAt() != null) {
            if (error != null) error.append("Mã OTP đã được sử dụng.");
            return false;
        }
        if (otp.getExpiresAt() == null || LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            if (error != null) error.append("Mã OTP đã hết hạn.");
            return false;
        }
        // mark used and activate user
        otp.setUsedAt(LocalDateTime.now());
        otpRepo.save(otp);
        kh.setStatus(1);
        khRepo.save(kh);
        return true;
    }

    @Transactional
    public boolean resendRegisterOtp(String email) {
        if (email == null || email.isBlank()) return false;
        KhachHang kh = khRepo.findByEmail(email);
        if (kh == null) return false;
        sendRegisterOtp(kh);
        return true;
    }

    // --- New helpers for pre-account OTP (session-based) ---
    public String generateOtp() {
        return generateNumericCode(6);
    }

    public void sendOtpEmail(String to, String otp) {
        String body = "Xin chào,\n\n" +
                "Mã xác thực (OTP) của bạn là: " + otp + "\n" +
                "Mã sẽ hết hạn sau 10 phút.\n\n" +
                "Trân trọng,\nAloTra";
        sendMail(to, "AloTra - Mã OTP xác nhận đăng ký", body);
    }
}