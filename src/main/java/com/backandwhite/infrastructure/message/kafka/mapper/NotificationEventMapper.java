package com.backandwhite.infrastructure.message.kafka.mapper;

import com.backandwhite.core.kafka.avro.EmailNotificationEvent;
import com.backandwhite.core.kafka.avro.SagaNotifyFailureEvent;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.domain.model.NotificationType;
import java.util.HashMap;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * MapStruct mapper that translates incoming Kafka Avro events to the
 * {@link Notification} domain model. It centralises the construction logic
 * previously spread across several consumers.
 */
@Mapper(componentModel = "spring")
public interface NotificationEventMapper {

    /**
     * Build a pending {@link Notification} from an {@link EmailNotificationEvent}.
     */
    default Notification toNotification(EmailNotificationEvent event) {
        if (event == null) {
            return null;
        }
        Map<String, Object> variables = new HashMap<>();
        if (event.getVariables() != null) {
            variables.putAll(event.getVariables());
        }
        return Notification.builder().recipient(str(event.getRecipient())).subject(str(event.getSubject()))
                .type(NotificationType.EMAIL).status(NotificationStatus.PENDING).variables(variables).retryCount(0)
                .build();
    }

    /**
     * Build a pending {@link Notification} from a {@link SagaNotifyFailureEvent}.
     */
    default Notification toNotification(SagaNotifyFailureEvent event) {
        if (event == null) {
            return null;
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderId", str(event.getOrderId()));
        variables.put("orderReference", str(event.getOrderReference()));
        variables.put("amount", str(event.getAmount()));
        variables.put("currency", str(event.getCurrency()));
        variables.put("reason", str(event.getReason()));

        return Notification.builder().recipient(str(event.getEmail()))
                .subject("Your order could not be processed — " + str(event.getOrderReference()))
                .type(NotificationType.EMAIL).status(NotificationStatus.PENDING).variables(variables).retryCount(0)
                .build();
    }

    /**
     * Build a {@link NotificationTemplate} fallback entry (file-based) from a
     * template name.
     */
    default NotificationTemplate toFileTemplate(String templateName) {
        if (templateName == null) {
            return null;
        }
        return NotificationTemplate.builder().name(templateName).templateFile("email/" + templateName).active(true)
                .build();
    }

    /**
     * Build a {@link Notification} from raw business-event data (used by
     * {@code BusinessEventNotificationConsumer}).
     */
    default Notification toNotification(String recipient, String subject, Map<String, Object> variables) {
        return Notification.builder().recipient(recipient).subject(subject).type(NotificationType.EMAIL)
                .status(NotificationStatus.PENDING).variables(variables).retryCount(0).build();
    }

    @Named("charSequenceToString")
    default String str(CharSequence cs) {
        return cs != null ? cs.toString() : null;
    }
}
