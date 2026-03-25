package com.backandwhite.api.dto.out;

import com.backandwhite.domain.model.NotificationType;
import lombok.*;

import java.time.Instant;

@Data
@With
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateDtoOut {

    private Long id;
    private String name;
    private String subject;
    private String templateFile;
    private NotificationType type;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
