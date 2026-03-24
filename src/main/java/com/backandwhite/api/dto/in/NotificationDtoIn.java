package com.backandwhite.api.dto.in;

import com.backandwhite.domain.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

@Data
@With
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDtoIn {

    @NotEmpty
    @Email
    @Schema(description = "Destinatario del correo", example = "usuario@ejemplo.com")
    private String recipient;

    @Schema(description = "Asunto del correo", example = "Confirmación de registro")
    private String subject;

    @NotNull
    @Schema(description = "Tipo de notificación", example = "EMAIL")
    private NotificationType type;

    @Schema(description = "ID del template a usar")
    private Long templateId;

    @Schema(description = "Variables a inyectar en el template")
    private Map<String, Object> variables;
}
