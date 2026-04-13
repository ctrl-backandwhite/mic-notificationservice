package com.backandwhite.infrastructure.message.kafka.consumer;

import com.backandwhite.application.service.EmailService;
import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.core.kafka.avro.SagaNotifyFailureEvent;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.domain.model.NotificationType;
import com.backandwhite.domain.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Consumes saga.order.notify-failure events and sends a payment failure
 * email to the customer as part of the Saga compensation flow.
 */
@Log4j2
@Service
@AllArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class SagaNotificationListener {

    private static final String TEMPLATE_NAME = "order-payment-failed";

    private final EmailService emailService;
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateUseCase notificationTemplateUseCase;

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_SAGA_ORDER_NOTIFY_FAILURE, groupId = AppConstants.KAFKA_GROUP_SAGA_NOTIFICATION, containerFactory = "avroKafkaListenerContainerFactory")
    public void onNotifyFailure(SagaNotifyFailureEvent event) {
        String orderId = str(event.getOrderId());
        String recipient = str(event.getEmail());
        log.info("::> [Saga] Received notify-failure: orderId={}, recipient={}", orderId, recipient);

        Map<String, Object> variables = new HashMap<>();
        variables.put("orderId", orderId);
        variables.put("orderReference", str(event.getOrderReference()));
        variables.put("amount", str(event.getAmount()));
        variables.put("currency", str(event.getCurrency()));
        variables.put("reason", str(event.getReason()));

        Notification notification = Notification.builder()
                .recipient(recipient)
                .subject("Tu pedido no pudo ser procesado — " + str(event.getOrderReference()))
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.PENDING)
                .variables(variables)
                .retryCount(0)
                .build();

        // Attempt to resolve template from DB; fall back to file template
        Optional<NotificationTemplate> templateOpt = notificationTemplateUseCase.findByName(TEMPLATE_NAME);
        if (templateOpt.isEmpty()) {
            log.warn("::> [Saga] Template '{}' not found in DB. Falling back to file template.", TEMPLATE_NAME);
            notification.setTemplate(NotificationTemplate.builder()
                    .name(TEMPLATE_NAME)
                    .templateFile("email/" + TEMPLATE_NAME)
                    .active(true)
                    .build());
        } else {
            NotificationTemplate template = templateOpt.get();
            if (Boolean.FALSE.equals(template.getActive())) {
                log.warn("::> [Saga] Template '{}' is inactive. Falling back to file template.", TEMPLATE_NAME);
                notification.setTemplate(NotificationTemplate.builder()
                        .name(TEMPLATE_NAME)
                        .templateFile("email/" + TEMPLATE_NAME)
                        .active(true)
                        .build());
            } else {
                notification.setTemplate(template);
            }
        }

        try {
            Notification saved = notificationRepository.save(notification);
            emailService.sendEmail(saved);
            log.info("::> [Saga] Failure notification sent for orderId={}", orderId);
        } catch (Exception e) {
            log.error("::> [Saga] Failed to send failure notification for orderId={}: {}",
                    orderId, e.getMessage(), e);
        }
    }

    private String str(CharSequence cs) {
        return cs != null ? cs.toString() : null;
    }
}
