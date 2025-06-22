package com.aston.app.controller;

import com.aston.app.service.EmailService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {

        logger.info("Received HTTP request to send email: to={}, subject={}, body={}", request.getTo(), request.getSubject(), request.getBody());
        emailService.sendEmail(request.getTo(), request.getSubject(), request.getBody());
        return ResponseEntity.ok("Email sent to: " + request.getTo() + " (see logs for details)");
    }

    @Data
    static class EmailRequest {
        private String to;
        private String subject;
        private String body;
    }
}