package com.backandwhite.api.dto.out;

import com.backandwhite.domain.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.*;

@Data
@With
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notification template response")
public class NotificationTemplateDtoOut {

    @Schema(description = "Template ID", example = "1")
    private Long id;

    @Schema(description = "Unique template name", example = "welcome")
    private String name;

    @Schema(description = "Email subject", example = "Welcome to NX036")
    private String subject;

    @Schema(description = "Thymeleaf template file path", example = "email/welcome")
    private String templateFile;

    @Schema(description = "Notification type", example = "EMAIL")
    private NotificationType type;

    @Schema(description = "Whether the template is active", example = "true")
    private Boolean active;

    @Schema(description = "Creation timestamp", example = "2026-04-16T11:00:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2026-04-16T12:00:00Z")
    private Instant updatedAt;

    @Schema(description = "Created by user", example = "system")
    private String createdBy;

    @Schema(description = "Updated by user", example = "system")
    private String updatedBy;
}
