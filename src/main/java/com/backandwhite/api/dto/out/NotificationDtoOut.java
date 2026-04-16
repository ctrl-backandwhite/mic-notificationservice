package com.backandwhite.api.dto.out;

import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import lombok.*;

@Data
@With
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notification response")
public class NotificationDtoOut {

    @Schema(description = "Notification ID", example = "1")
    private Long id;

    @Schema(description = "Email recipient", example = "user@example.com")
    private String recipient;

    @Schema(description = "Email subject", example = "Welcome to NX036")
    private String subject;

    @Schema(description = "Notification type", example = "EMAIL")
    private NotificationType type;

    @Schema(description = "Notification status", example = "SENT")
    private NotificationStatus status;

    @Schema(description = "Associated template")
    private NotificationTemplateDtoOut template;

    @Schema(description = "Template variables")
    private Map<String, Object> variables;

    @Schema(description = "Error message if sending failed", example = "SMTP connection timeout")
    private String errorMessage;

    @Schema(description = "Number of retry attempts", example = "0")
    private Integer retryCount;

    @Schema(description = "Timestamp when notification was sent", example = "2026-04-16T12:00:00Z")
    private Instant sentAt;

    @Schema(description = "Creation timestamp", example = "2026-04-16T11:00:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2026-04-16T12:00:00Z")
    private Instant updatedAt;

    @Schema(description = "Created by user", example = "system")
    private String createdBy;

    @Schema(description = "Updated by user", example = "system")
    private String updatedBy;
}
