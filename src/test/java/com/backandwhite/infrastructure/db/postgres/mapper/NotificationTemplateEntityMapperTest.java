package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.infrastructure.db.postgres.entity.NotificationTemplateEntity;
import com.backandwhite.provider.NotificationTemplateProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = { NotificationTemplateEntityMapperImpl.class })
class NotificationTemplateEntityMapperTest {

    @Autowired
    private NotificationTemplateEntityMapper mapper;

    @Test
    void toDomain_mapsAllFields() {
        NotificationTemplateEntity entity = NotificationTemplateProvider.templateEntity();

        NotificationTemplate domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getName()).isEqualTo(entity.getName());
        assertThat(domain.getSubject()).isEqualTo(entity.getSubject());
        assertThat(domain.getTemplateFile()).isEqualTo(entity.getTemplateFile());
        assertThat(domain.getType()).isEqualTo(entity.getType());
        assertThat(domain.getActive()).isEqualTo(entity.getActive());
        assertThat(domain.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(domain.getCreatedBy()).isEqualTo(entity.getCreatedBy());
    }

    @Test
    void toEntity_mapsAllFields_ignoringAudit() {
        NotificationTemplate domain = NotificationTemplateProvider.template();

        NotificationTemplateEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getName()).isEqualTo(domain.getName());
        assertThat(entity.getSubject()).isEqualTo(domain.getSubject());
        assertThat(entity.getTemplateFile()).isEqualTo(domain.getTemplateFile());
        assertThat(entity.getType()).isEqualTo(domain.getType());
        assertThat(entity.getActive()).isEqualTo(domain.getActive());
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }
}
