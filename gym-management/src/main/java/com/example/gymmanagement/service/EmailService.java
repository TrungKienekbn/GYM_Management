package com.example.gymmanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import lombok.RequiredArgsConstructor;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Async
    public void sendWelcomeEmail(String email, String name, String verificationToken) {
        String verifyUrl = baseUrl + "/api/auth/verify-email?token=" + verificationToken;
        log.info("[EMAIL] Welcome email to {} - Verify: {}", email, verifyUrl);
        // In production: integrate with SMTP/SendGrid/Mailgun
    }

    @Async
    public void sendWorkoutReminder(String email, String name, String sessionDate, String planName) {
        log.info("[EMAIL] Workout reminder to {} - Session: {} on {}", email, planName, sessionDate);
        // In production: send actual email
    }

    @Async
    public void sendMembershipConfirmation(String email, String name, String membershipType, String endDate) {
        log.info("[EMAIL] Membership confirmation to {} - Type: {}, Expires: {}", email, membershipType, endDate);
    }

    @Async
    public void sendMembershipExpiryReminder(String email, String name, String expiryDate) {
        log.info("[EMAIL] Membership expiry reminder to {} - Expires: {}", email, expiryDate);
    }

    @Async
    public void sendPromotionalEmail(String email, String name, String subject, String message) {
        log.info("[EMAIL] Promotional email to {} - Subject: {}", email, subject);
    }
    @Async
    public void sendPosOrderConfirmation(String email, String customerName, Long orderId, Double total) {
        log.info("[EMAIL] Hóa đơn bán tại quầy #{} gửi tới {} ({}) - Tổng tiền: {}", orderId, email, customerName, total);
        // In production: gửi email kèm PDF/link hóa đơn
    }
    @Async
    public void sendOrderCancellationOtp(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Mã OTP hủy đơn hàng GYMPRO");
        message.setText("Mã OTP hủy đơn của bạn là " + code + ". Mã có hiệu lực trong 5 phút và chỉ dùng một lần.");
        mailSender.send(message);
    }
}
