package com.backandwhite.api.dto.in;

import lombok.*;

import java.util.Map;

/**
 * Evento Kafka para solicitar el envío de una notificación por correo.
 * Publicado por otros microservicios en el topic "notification.email.send".
 */
@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationEvent {

    private String recipient;
    private String subject;
    private String templateName;
    private Map<String, Object> variables;
}
