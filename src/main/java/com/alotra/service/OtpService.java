package com.alotra.service;

import com.alotra.entity.Customer;
import com.alotra.entity.OtpCode;
import com.alotra.entity.enums.CustomerStatus;
import com.alotra.repository.CustomerRepository;
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
    private final CustomerRepository customerRepo;
    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final SecureRandom random = new SecureRandom();

    public OtpService(OtpCodeRepository otpRepo,
                      CustomerRepository customerRepo,
                      JavaMailSender mailSender,
                      @Value("${app.mail.from:${spring.mail.username}}") String fromAddress) {
        this.otpRepo = otpRepo;
        this.customerRepo = customerRepo;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public String generateNumericCode(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    @Transactional
    public void sendRegisterOtp(Customer customer) {
        otpRepo.deleteByCustomerAndTypeAndExpiresAtBefore(customer, TYPE_REGISTER, LocalDateTime.now().minusDays(1));
        String code = generateNumericCode(6);
        OtpCode otp = new OtpCode();
        otp.setCustomer(customer);
        otp.setType(TYPE_REGISTER);
        otp.setCode(code);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepo.save(otp);
        sendMail(customer.getEmail(), "AloTra - Mã xác thực đăng ký", buildRegisterMailBody(customer, code));
    }

    public String buildRegisterMailBody(Customer customer, String code) {
        String name = customer.getFullName() != null ? customer.getFullName() : customer.getUsername();
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
        if (fromAddress != null && !fromAddress.isBlank()) {
            msg.setFrom(fromAddress.trim());
        }
        try {
            mailSender.send(msg);
        } catch (org.springframework.mail.MailException ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Transactional
    public boolean verifyRegisterOtp(String email, String code, StringBuilder error) {
        if (email == null || code == null || code.isBlank()) {
            if (error != null) error.append("Thiếu email hoặc mã OTP.");
            return false;
        }
        Customer customer = customerRepo.findByEmail(email).orElse(null);
        if (customer == null) {
            if (error != null) error.append("Không tìm thấy tài khoản theo email.");
            return false;
        }
        Optional<OtpCode> opt = otpRepo.findTopByCustomerAndTypeAndCodeOrderByIdDesc(customer, TYPE_REGISTER, code.trim());
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
        otp.setUsedAt(LocalDateTime.now());
        otpRepo.save(otp);
        customer.setStatus(CustomerStatus.ACTIVE);
        customerRepo.save(customer);
        return true;
    }

    @Transactional
    public boolean resendRegisterOtp(String email) {
        if (email == null || email.isBlank()) return false;
        Customer customer = customerRepo.findByEmail(email).orElse(null);
        if (customer == null) return false;
        sendRegisterOtp(customer);
        return true;
    }

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