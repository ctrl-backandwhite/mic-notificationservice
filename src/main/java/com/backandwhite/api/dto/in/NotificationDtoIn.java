package com.backandwhite.api.dto.in;

import com.backandwhite.domain.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.*;

@Data
@With
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDtoIn {

    @NotEmpty
    @Email
    @Schema(description = "Email recipient", example = "user@example.com")
    private String recipient;

    @Schema(description = "Email subject", example = "Registration confirmation")
    private String subject;

    @NotNull
    @Schema(description = "Notification type", example = "EMAIL")
    private NotificationType type;

    @Schema(description = "Template ID to use", example = "1")
    private Long templateId;

    @Schema(description = "Variables to inject into the template", example = "{\"name\": \"John\", \"code\": \"123456\"}")
    private Map<String, Object> variables;
}
