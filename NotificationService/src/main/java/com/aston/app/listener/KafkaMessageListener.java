package com.aston.app.listener;

import com.aston.app.service.EmailService;
import com.aston.app.service.UserEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageListener.class);

    private final EmailService emailService;

    @Value("${email.subject.create}")
    private String createSubject;

    @Value("${email.body.create}")
    private String createBody;

    @Value("${email.subject.delete}")
    private String deleteSubject;

    @Value("${email.body.delete}")
    private String deleteBody;

    @KafkaListener(topics = "${kafka.topic.user}", groupId = "notification-group")
    public void listenUserEvents(UserEvent event) {
        logger.info("Received user event: {}", event);

        String email = event.getEmail();
        String operation = event.getOperation();

        try {
            switch (operation) {
                case "create":
                    emailService.sendEmail(email, createSubject, createBody);
                    break;
                case "delete":
                    emailService.sendEmail(email, deleteSubject, deleteBody);
                    break;
                default:
                    logger.warn("Unknown operation type: {}", operation);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error processing event: ", e);
        }
    }
}