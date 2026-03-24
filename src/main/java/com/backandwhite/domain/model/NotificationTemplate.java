package com.backandwhite.domain.model;

import lombok.*;
import java.time.Instant;

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
