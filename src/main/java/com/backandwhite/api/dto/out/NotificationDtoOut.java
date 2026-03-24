package com.backandwhite.api.dto.out;

import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.model.NotificationType;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Data
@With
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDtoOut {

    private Long id;
    private String recipient;
    private String subject;
    private NotificationType type;
    private NotificationStatus status;
    private NotificationTemplateDtoOut template;
    private Map<String, Object> variables;
    private String errorMessage;
    private Integer retryCount;
    private Instant sentAt;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
