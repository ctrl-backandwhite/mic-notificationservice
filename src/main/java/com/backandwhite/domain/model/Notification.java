package com.backandwhite.domain.model;

import lombok.*;
import java.time.Instant;
import java.util.Map;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Notification {

    private Long id;
    private String recipient;
    private String subject;
    private NotificationType type;
    private NotificationStatus status;
    private NotificationTemplate template;
    private Map<String, Object> variables;
    private String errorMessage;
    private Integer retryCount;
    private Instant sentAt;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
