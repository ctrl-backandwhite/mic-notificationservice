package com.backandwhite.provider;

import com.backandwhite.api.dto.in.NotificationDtoIn;
import com.backandwhite.api.dto.out.NotificationDtoOut;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.model.NotificationType;
import com.backandwhite.infrastructure.db.postgres.entity.NotificationEntity;

import java.time.Instant;
import java.util.Map;

public final class NotificationProvider {

    public static final Long NOTIFICATION_ID = 1L;
    public static final String RECIPIENT = "usuario@ejemplo.com";
    public static final String SUBJECT = "Bienvenido a nuestra plataforma";
    public static final NotificationType TYPE = NotificationType.EMAIL;
    public static final NotificationStatus STATUS = NotificationStatus.SENT;
    public static final Instant SENT_AT = Instant.parse("2025-01-01T10:00:00Z");
    public static final Integer RETRY_COUNT = 0;

    public static final Long OTHER_NOTIFICATION_ID = 2L;
    public static final String OTHER_RECIPIENT = "otro@ejemplo.com";
    public static final NotificationStatus OTHER_STATUS = NotificationStatus.FAILED;

    private NotificationProvider() {
    }

    public static Notification notification() {
        return Notification.builder()
                .id(NOTIFICATION_ID)
                .recipient(RECIPIENT)
                .subject(SUBJECT)
                .type(TYPE)
                .status(STATUS)
                .template(NotificationTemplateProvider.template())
                .variables(Map.of("name", "Usuario"))
                .retryCount(RETRY_COUNT)
                .sentAt(SENT_AT)
                .createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT)
                .createdBy(AuditProvider.CREATED_BY)
                .updatedBy(AuditProvider.UPDATED_BY)
                .build();
    }

    public static Notification failedNotification() {
        return Notification.builder()
                .id(OTHER_NOTIFICATION_ID)
                .recipient(OTHER_RECIPIENT)
                .subject(SUBJECT)
                .type(TYPE)
                .status(OTHER_STATUS)
                .errorMessage("SMTP connection refused")
                .retryCount(3)
                .createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT)
                .createdBy(AuditProvider.CREATED_BY)
                .updatedBy(AuditProvider.UPDATED_BY)
                .build();
    }

    public static NotificationEntity notificationEntity() {
        return NotificationEntity.builder()
                .id(NOTIFICATION_ID)
                .recipient(RECIPIENT)
                .subject(SUBJECT)
                .type(TYPE)
                .status(STATUS)
                .template(NotificationTemplateProvider.templateEntity())
                .variables(Map.of("name", "Usuario"))
                .retryCount(RETRY_COUNT)
                .sentAt(SENT_AT)
                .createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT)
                .createdBy(AuditProvider.CREATED_BY)
                .updatedBy(AuditProvider.UPDATED_BY)
                .build();
    }

    public static NotificationDtoIn notificationDtoIn() {
        return NotificationDtoIn.builder()
                .recipient(RECIPIENT)
                .subject(SUBJECT)
                .type(TYPE)
                .templateId(NotificationTemplateProvider.TEMPLATE_ID)
                .variables(Map.of("name", "Usuario"))
                .build();
    }

    public static NotificationDtoOut notificationDtoOut(Long id) {
        return NotificationDtoOut.builder()
                .id(id)
                .recipient(RECIPIENT)
                .subject(SUBJECT)
                .type(TYPE)
                .status(NotificationStatus.PENDING)
                .template(NotificationTemplateProvider.templateDtoOut(NotificationTemplateProvider.TEMPLATE_ID))
                .variables(Map.of("name", "Usuario"))
                .retryCount(RETRY_COUNT)
                .sentAt(SENT_AT)
                .createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT)
                .createdBy(AuditProvider.CREATED_BY)
                .updatedBy(AuditProvider.UPDATED_BY)
                .build();
    }
}
