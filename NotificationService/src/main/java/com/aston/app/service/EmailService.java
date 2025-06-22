package com.aston.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender emailSender;

    @Async
    public void sendEmail(String to, String subject, String body) {
        String emailId = UUID.randomUUID().toString();

        if (emailSender == null) {
            logger.warn("[{}] JavaMailSender is not configured. Email will not be sent. Check spring.mail.* configuration or disable email sending.", emailId);
            logger.info("[{}] Simulating email send: to={}, subject={}, body={}", emailId, to, subject, body);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            emailSender.send(message);
            logger.info("[{}] Email successfully sent to: {}", emailId, to);
        } catch (Exception e) {
            logger.error("[{}] Error sending email to: {}: ", emailId, to, e);
        }
    }
}