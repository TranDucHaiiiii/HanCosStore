package com.example.demodatn2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReturnNotificationService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Async("returnNotificationExecutor")
    public void notifyStatus(String toEmail, String subject, String message) {
        try {
            if (toEmail == null || toEmail.isBlank()) {
                return;
            }

            SimpleMailMessage mail = new SimpleMailMessage();
            if (mailFrom != null && !mailFrom.isBlank()) {
                mail.setFrom(mailFrom);
            }
            mail.setTo(toEmail);
            mail.setSubject(subject);
            mail.setText(message);
            mailSender.send(mail);
        } catch (Exception ignored) {
            // Notification failure must not roll back the return workflow.
        }
    }
}
