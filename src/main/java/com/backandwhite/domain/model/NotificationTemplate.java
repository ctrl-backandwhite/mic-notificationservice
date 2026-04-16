package com.backandwhite.domain.model;

import java.time.Instant;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class NotificationTemplate {

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
