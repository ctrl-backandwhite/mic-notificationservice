package com.backandwhite.application.service;

import com.backandwhite.api.dto.in.EmailNotificationEvent;
import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.model.NotificationType;
import com.backandwhite.domain.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
@AllArgsConstructor
public class KafkaNotificationConsumer {

    public static final String TOPIC = "notification.email.send";

    private final EmailService emailService;
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateUseCase notificationTemplateUseCase;

    @KafkaListener(topics = TOPIC, groupId = "${spring.application.name}")
    public void consume(EmailNotificationEvent event) {
        log.debug("::> Received notification event for recipient: {}", event.getRecipient());

        Notification notification = Notification.builder()
                .recipient(event.getRecipient())
                .subject(event.getSubject())
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.PENDING)
                .variables(event.getVariables() != null ? event.getVariables() : new HashMap<>())
                .retryCount(0)
                .build();

        if (event.getTemplateName() != null) {
            notificationTemplateUseCase.findByName(event.getTemplateName())
                    .ifPresent(template -> {
                        notification.setTemplate(template);
                        if (notification.getSubject() == null || notification.getSubject().isBlank()) {
                            notification.setSubject(template.getSubject());
                        }
                    });
        }

        Notification saved = notificationRepository.save(notification);
        log.debug("::> Notification persisted with id: {}", saved.getId());

        emailService.sendEmail(saved);
    }
}
