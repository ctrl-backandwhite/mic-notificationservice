package com.backandwhite.infrastructure.message.kafka.consumer;

import com.backandwhite.application.handler.NotificationCommandHandler;
import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.core.kafka.avro.EmailNotificationEvent;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.domain.model.NotificationType;
import com.backandwhite.domain.port.NotificationSender;
import com.backandwhite.domain.repository.NotificationRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@AllArgsConstructor
public class KafkaNotificationConsumer {

    private final NotificationSender notificationSender;
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateUseCase notificationTemplateUseCase;
    private final NotificationCommandHandler notificationCommandHandler;

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_NOTIFICATION_EMAIL, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void consume(EmailNotificationEvent event) {
        log.debug("::> Received notification event for template: {}", event.getTemplateName());
        processEvent(event);
    }

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_CMS_CONTACT_MESSAGE_RECEIVED, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void consumeContactMessage(EmailNotificationEvent event) {
        log.debug("::> Received CMS contact message event");
        processEvent(event);
    }

    /**
     * Common notification event processing logic. Builds the domain object,
     * validates, resolves the template, and sends the email.
     */
    private void processEvent(EmailNotificationEvent event) {
        // Convert Map<String, String> (Avro) to Map<String, Object> (domain)
        Map<String, Object> variables = new HashMap<>();
        if (event.getVariables() != null) {
            variables.putAll(event.getVariables());
        }

        Notification notification = Notification.builder()
                .recipient(event.getRecipient() != null ? event.getRecipient() : null)
                .subject(event.getSubject() != null ? event.getSubject() : null).type(NotificationType.EMAIL)
                .status(NotificationStatus.PENDING).variables(variables).retryCount(0).build();

        // Validate notification before persisting
        notificationCommandHandler.validate(notification);

        // Resolve template — file-based templates are NOT set on notification
        // before save to avoid TransientPropertyValueException
        String templateName = event.getTemplateName() != null ? event.getTemplateName() : null;
        NotificationTemplate fileTemplate = resolveTemplate(notification, templateName);

        Notification saved = notificationRepository.save(notification);
        log.debug("::> Notification persisted with id: {}", saved.getId());

        // Restore file-based template after persist (needed for email rendering)
        if (fileTemplate != null) {
            saved.setTemplate(fileTemplate);
        }

        notificationSender.send(saved);
    }

    /**
     * Resolves the notification template. If found in DB, sets it directly on the
     * notification (managed entity). If not found, returns a transient file-based
     * template that must be set AFTER persisting the notification.
     */
    private NotificationTemplate resolveTemplate(Notification notification, String templateName) {
        if (templateName == null) {
            return null;
        }
        Optional<NotificationTemplate> templateOpt = notificationTemplateUseCase.findByName(templateName);
        if (templateOpt.isEmpty()) {
            log.info("::> Template '{}' not found in DB. Falling back to file: email/{}", templateName, templateName);
            return NotificationTemplate.builder().name(templateName).templateFile("email/" + templateName).active(true)
                    .build();
        }
        NotificationTemplate template = templateOpt.get();
        if (Boolean.FALSE.equals(template.getActive())) {
            log.warn("::> Notification template '{}' is inactive. Using default template.", templateName);
            return null;
        }
        notification.setTemplate(template);
        if (notification.getSubject() == null || notification.getSubject().isBlank()) {
            notification.setSubject(template.getSubject());
        }
        return null;
    }
}
