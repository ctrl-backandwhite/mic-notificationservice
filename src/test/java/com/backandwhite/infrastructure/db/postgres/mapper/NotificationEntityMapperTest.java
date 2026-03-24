package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.Notification;
import com.backandwhite.infrastructure.db.postgres.entity.NotificationEntity;
import com.backandwhite.provider.NotificationProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = { NotificationEntityMapperImpl.class, NotificationTemplateEntityMapperImpl.class })
class NotificationEntityMapperTest {

    @Autowired
    private NotificationEntityMapper mapper;

    @Test
    void toDomain_mapsAllFields() {
        NotificationEntity entity = NotificationProvider.notificationEntity();

        Notification domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getRecipient()).isEqualTo(entity.getRecipient());
        assertThat(domain.getSubject()).isEqualTo(entity.getSubject());
        assertThat(domain.getType()).isEqualTo(entity.getType());
        assertThat(domain.getStatus()).isEqualTo(entity.getStatus());
        assertThat(domain.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(domain.getCreatedBy()).isEqualTo(entity.getCreatedBy());
    }

    @Test
    void toEntity_mapsAllFields_ignoringAudit() {
        Notification domain = NotificationProvider.notification();

        NotificationEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getRecipient()).isEqualTo(domain.getRecipient());
        assertThat(entity.getSubject()).isEqualTo(domain.getSubject());
        assertThat(entity.getType()).isEqualTo(domain.getType());
        assertThat(entity.getStatus()).isEqualTo(domain.getStatus());
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }
}
