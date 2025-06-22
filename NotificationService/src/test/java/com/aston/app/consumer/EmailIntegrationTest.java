package com.aston.app.consumer;

import com.aston.app.ConsumerApplication;
import com.aston.app.service.UserEvent;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ConsumerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "spring.mail.username=test@example.com",
        "spring.mail.password=password",
        "email.subject.create=Account Created",
        "email.body.create=Your account has been created.",
        "email.subject.delete=Account Deleted",
        "email.body.delete=Your account has been deleted."
})
@Testcontainers
class EmailIntegrationTest {

    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Value("${kafka.topic.user}")
    private String topic;

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.ALL)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("test@example.com", "password"))
            .withPerMethodLifecycle(true);

    private static final String KAFKA_IMAGE = "confluentinc/cp-kafka:7.3.0";

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE));

    @BeforeAll
    static void setup() {
        System.setProperty("spring.kafka.bootstrap-servers", kafka.getBootstrapServers());
    }

    @Test
    void testUserCreationEmail() throws Exception {
        String userEmail = "newuser@example.com";
        UserEvent userEvent = new UserEvent("create", userEmail);

        CompletableFuture<SendResult<String, UserEvent>> future = kafkaTemplate.send(topic, userEvent);

        future.get(10, TimeUnit.SECONDS);

        greenMail.waitForIncomingEmail(5000, 1);
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages).hasSize(1);

        MimeMessage message = receivedMessages[0];
        assertThat(message.getSubject()).isEqualTo("Account Created");
        assertThat(GreenMailUtil.getBody(message)).isEqualTo("Your account has been created.");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(userEmail);
    }

    @Test
    void testUserDeletionEmail() throws Exception {
        String userEmail = "olduser@example.com";
        UserEvent userEvent = new UserEvent("delete", userEmail);

        CompletableFuture<SendResult<String, UserEvent>> future = kafkaTemplate.send(topic, userEvent);

        future.get(10, TimeUnit.SECONDS);

        greenMail.waitForIncomingEmail(5000, 1);
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages).hasSize(1);

        MimeMessage message = receivedMessages[0];
        assertThat(message.getSubject()).isEqualTo("Account Deleted");
        assertThat(GreenMailUtil.getBody(message)).isEqualTo("Your account has been deleted.");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(userEmail);
    }
}
