package com.backandwhite.api.mapper;

import com.backandwhite.api.dto.in.NotificationTemplateDtoIn;
import com.backandwhite.api.dto.out.NotificationTemplateDtoOut;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.provider.NotificationTemplateProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = { NotificationTemplateDtoMapperImpl.class })
class NotificationTemplateDtoMapperTest {

    @Autowired
    private NotificationTemplateDtoMapper mapper;

    @Test
    void toDtoOut_mapsAllFields() {
        NotificationTemplate domain = NotificationTemplateProvider.template();

        NotificationTemplateDtoOut dtoOut = mapper.toDtoOut(domain);

        assertThat(dtoOut.getId()).isEqualTo(domain.getId());
        assertThat(dtoOut.getName()).isEqualTo(domain.getName());
        assertThat(dtoOut.getSubject()).isEqualTo(domain.getSubject());
        assertThat(dtoOut.getTemplateFile()).isEqualTo(domain.getTemplateFile());
        assertThat(dtoOut.getType()).isEqualTo(domain.getType());
        assertThat(dtoOut.getActive()).isEqualTo(domain.getActive());
        assertThat(dtoOut.getCreatedAt()).isEqualTo(domain.getCreatedAt());
        assertThat(dtoOut.getCreatedBy()).isEqualTo(domain.getCreatedBy());
    }

    @Test
    void toDomain_ignoresIdAndAudit() {
        NotificationTemplateDtoIn dtoIn = NotificationTemplateProvider.templateDtoIn();

        NotificationTemplate domain = mapper.toDomain(dtoIn);

        assertThat(domain.getName()).isEqualTo(dtoIn.getName());
        assertThat(domain.getSubject()).isEqualTo(dtoIn.getSubject());
        assertThat(domain.getTemplateFile()).isEqualTo(dtoIn.getTemplateFile());
        assertThat(domain.getType()).isEqualTo(dtoIn.getType());
        assertThat(domain.getId()).isNull();
        assertThat(domain.getCreatedAt()).isNull();
    }
}
