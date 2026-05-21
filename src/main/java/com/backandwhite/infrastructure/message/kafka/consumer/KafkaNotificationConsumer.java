package com.backandwhite.infrastructure.message.kafka.consumer;

import com.backandwhite.application.handler.NotificationCommandHandler;
import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.core.kafka.avro.EmailNotificationEvent;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.domain.port.NotificationSender;
import com.backandwhite.domain.repository.NotificationRepository;
import com.backandwhite.infrastructure.message.kafka.mapper.NotificationEventMapper;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ClassPathResource;
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
    private final NotificationEventMapper notificationEventMapper;

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
        // Drop events that don't have a real template wired up so users never
        // receive a blank "you have a notification" email. A template must
        // exist either in the DB (notification_templates row) or as a
        // Thymeleaf file under templates/email/<name>.html.
        String templateName = event.getTemplateName();
        if (templateName == null || templateName.isBlank()) {
            log.info("::> Notification skipped — empty templateName");
            return;
        }
        Optional<NotificationTemplate> templateOpt = notificationTemplateUseCase.findByName(templateName);
        boolean fileExists = new ClassPathResource("templates/email/" + templateName + ".html").exists();
        if (templateOpt.isEmpty() && !fileExists) {
            log.info("::> Notification skipped — no template for '{}' (DB+file both missing)", templateName);
            return;
        }

        Notification notification = notificationEventMapper.toNotification(event);

        // Validate notification before persisting
        notificationCommandHandler.validate(notification);

        // Resolve template — file-based templates are NOT set on notification
        // before save to avoid TransientPropertyValueException
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
            return notificationEventMapper.toFileTemplate(templateName);
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
