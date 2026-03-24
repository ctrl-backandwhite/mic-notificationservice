package com.backandwhite.provider;

import com.backandwhite.api.dto.in.NotificationTemplateDtoIn;
import com.backandwhite.api.dto.out.NotificationTemplateDtoOut;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.domain.model.NotificationType;
import com.backandwhite.infrastructure.db.postgres.entity.NotificationTemplateEntity;

public final class NotificationTemplateProvider {

    public static final Long TEMPLATE_ID = 1L;
    public static final String TEMPLATE_NAME = "welcome-email";
    public static final String TEMPLATE_SUBJECT = "Bienvenido a nuestra plataforma";
    public static final String TEMPLATE_FILE = "email/welcome";
    public static final NotificationType TEMPLATE_TYPE = NotificationType.EMAIL;
    public static final Boolean TEMPLATE_ACTIVE = true;

    public static final Long OTHER_TEMPLATE_ID = 2L;
    public static final String OTHER_TEMPLATE_NAME = "password-reset";
    public static final String OTHER_TEMPLATE_SUBJECT = "Recuperación de contraseña";
    public static final String OTHER_TEMPLATE_FILE = "email/password-reset";

    private NotificationTemplateProvider() {
    }

    public static NotificationTemplate template() {
        return NotificationTemplate.builder()
                .id(TEMPLATE_ID)
                .name(TEMPLATE_NAME)
                .subject(TEMPLATE_SUBJECT)
                .templateFile(TEMPLATE_FILE)
                .type(TEMPLATE_TYPE)
                .active(TEMPLATE_ACTIVE)
                .createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT)
                .createdBy(AuditProvider.CREATED_BY)
                .updatedBy(AuditProvider.UPDATED_BY)
                .build();
    }

    public static NotificationTemplate otherTemplate() {
        return NotificationTemplate.builder()
                .id(OTHER_TEMPLATE_ID)
                .name(OTHER_TEMPLATE_NAME)
                .subject(OTHER_TEMPLATE_SUBJECT)
                .templateFile(OTHER_TEMPLATE_FILE)
                .type(TEMPLATE_TYPE)
                .active(TEMPLATE_ACTIVE)
                .createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT)
                .createdBy(AuditProvider.CREATED_BY)
                .updatedBy(AuditProvider.UPDATED_BY)
                .build();
    }

    public static NotificationTemplateEntity templateEntity() {
        return NotificationTemplateEntity.builder()
                .id(TEMPLATE_ID)
                .name(TEMPLATE_NAME)
                .subject(TEMPLATE_SUBJECT)
                .templateFile(TEMPLATE_FILE)
                .type(TEMPLATE_TYPE)
                .active(TEMPLATE_ACTIVE)
                .createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT)
                .createdBy(AuditProvider.CREATED_BY)
                .updatedBy(AuditProvider.UPDATED_BY)
                .build();
    }

    public static NotificationTemplateDtoIn templateDtoIn() {
        return NotificationTemplateDtoIn.builder()
                .name(TEMPLATE_NAME)
                .subject(TEMPLATE_SUBJECT)
                .templateFile(TEMPLATE_FILE)
                .type(TEMPLATE_TYPE)
                .active(TEMPLATE_ACTIVE)
                .build();
    }

    public static NotificationTemplateDtoOut templateDtoOut(Long id) {
        return NotificationTemplateDtoOut.builder()
                .id(id)
                .name(TEMPLATE_NAME)
                .subject(TEMPLATE_SUBJECT)
                .templateFile(TEMPLATE_FILE)
                .type(TEMPLATE_TYPE)
                .active(TEMPLATE_ACTIVE)
                .createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT)
                .createdBy(AuditProvider.CREATED_BY)
                .updatedBy(AuditProvider.UPDATED_BY)
                .build();
    }
}
