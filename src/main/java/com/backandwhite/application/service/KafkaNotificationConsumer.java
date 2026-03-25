package com.backandwhite.application.service;

import com.backandwhite.application.handler.NotificationCommandHandler;
import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.core.kafka.avro.EmailNotificationEvent;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.domain.model.NotificationType;
import com.backandwhite.domain.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Log4j2
@Service
@AllArgsConstructor
public class KafkaNotificationConsumer {

    private final EmailService emailService;
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateUseCase notificationTemplateUseCase;
    private final NotificationCommandHandler notificationCommandHandler;

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_NOTIFICATION_EMAIL, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void consume(EmailNotificationEvent event) {
        log.debug("::> Received notification event for recipient: {}", event.getRecipient());
        processEvent(event);
    }

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_CMS_CONTACT_MESSAGE_RECEIVED, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void consumeContactMessage(EmailNotificationEvent event) {
        log.debug("::> Received CMS contact message event for recipient: {}", event.getRecipient());
        processEvent(event);
    }

    /**
     * Lógica común de procesamiento de eventos de notificación.
     * Construye el dominio, valida, resuelve el template y envía el correo.
     */
    private void processEvent(EmailNotificationEvent event) {
        // Convertir Map<String, String> (Avro) a Map<String, Object> (dominio)
        Map<String, Object> variables = new HashMap<>();
        if (event.getVariables() != null) {
            variables.putAll(event.getVariables());
        }

        Notification notification = Notification.builder()
                .recipient(event.getRecipient() != null ? event.getRecipient() : null)
                .subject(event.getSubject() != null ? event.getSubject() : null)
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.PENDING)
                .variables(variables)
                .retryCount(0)
                .build();

        // Validar notificación antes de persistir
        notificationCommandHandler.validate(notification);

        // Resolver template, verificar active y loguear warning si no se encuentra
        String templateName = event.getTemplateName() != null ? event.getTemplateName().toString() : null;
        if (templateName != null) {
            Optional<NotificationTemplate> templateOpt = notificationTemplateUseCase.findByName(templateName);
            if (templateOpt.isEmpty()) {
                log.warn("::> Notification template '{}' not found in database. Using default template.", templateName);
            } else {
                NotificationTemplate template = templateOpt.get();
                if (Boolean.FALSE.equals(template.getActive())) {
                    log.warn("::> Notification template '{}' is inactive. Using default template.", templateName);
                } else {
                    notification.setTemplate(template);
                    if (notification.getSubject() == null || notification.getSubject().isBlank()) {
                        notification.setSubject(template.getSubject());
                    }
                }
            }
        }

        Notification saved = notificationRepository.save(notification);
        log.debug("::> Notification persisted with id: {}", saved.getId());

        emailService.sendEmail(saved);
    }
}
