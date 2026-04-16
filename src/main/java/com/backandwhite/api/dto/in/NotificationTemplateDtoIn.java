package com.backandwhite.api.dto.in;

import com.backandwhite.domain.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@With
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateDtoIn {

    @NotEmpty
    @Schema(description = "Unique template name", example = "welcome-email")
    private String name;

    @NotEmpty
    @Schema(description = "Email subject", example = "Welcome to our platform")
    private String subject;

    @NotEmpty
    @Schema(description = "Thymeleaf template file name (without extension)", example = "email/welcome")
    private String templateFile;

    @NotNull
    @Schema(description = "Notification type", example = "EMAIL")
    private NotificationType type;

    @Schema(description = "Indicates whether the template is active", example = "true", defaultValue = "true")
    private Boolean active;
}
