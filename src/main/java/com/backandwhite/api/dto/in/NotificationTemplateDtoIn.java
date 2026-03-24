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
    @Schema(description = "Nombre único del template", example = "welcome-email")
    private String name;

    @NotEmpty
    @Schema(description = "Asunto del correo electrónico", example = "Bienvenido a nuestra plataforma")
    private String subject;

    @NotEmpty
    @Schema(description = "Nombre del archivo de template Thymeleaf (sin extensión)", example = "email/welcome")
    private String templateFile;

    @NotNull
    @Schema(description = "Tipo de notificación", example = "EMAIL")
    private NotificationType type;

    @Schema(description = "Indica si el template está activo", example = "true", defaultValue = "true")
    private Boolean active;
}
