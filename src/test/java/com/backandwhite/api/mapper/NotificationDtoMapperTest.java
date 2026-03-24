package com.backandwhite.api.mapper;

import com.backandwhite.api.dto.in.NotificationDtoIn;
import com.backandwhite.api.dto.out.NotificationDtoOut;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.provider.NotificationProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(classes = { NotificationDtoMapperImpl.class, NotificationTemplateDtoMapperImpl.class })
class NotificationDtoMapperTest {

    @Autowired
    private NotificationDtoMapper mapper;

    @Test
    void toDtoOut_mapsAllFields() {
        Notification domain = NotificationProvider.notification();

        NotificationDtoOut dtoOut = mapper.toDtoOut(domain);

        assertThat(dtoOut.getId()).isEqualTo(domain.getId());
        assertThat(dtoOut.getRecipient()).isEqualTo(domain.getRecipient());
        assertThat(dtoOut.getSubject()).isEqualTo(domain.getSubject());
        assertThat(dtoOut.getType()).isEqualTo(domain.getType());
        assertThat(dtoOut.getStatus()).isEqualTo(domain.getStatus());
        assertThat(dtoOut.getCreatedAt()).isEqualTo(domain.getCreatedAt());
    }

    @Test
    void toDomain_setsStatusToPending_andIgnoresAudit() {
        NotificationDtoIn dtoIn = NotificationProvider.notificationDtoIn();

        Notification domain = mapper.toDomain(dtoIn);

        assertThat(domain.getRecipient()).isEqualTo(dtoIn.getRecipient());
        assertThat(domain.getSubject()).isEqualTo(dtoIn.getSubject());
        assertThat(domain.getType()).isEqualTo(dtoIn.getType());
        assertThat(domain.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(domain.getId()).isNull();
        assertThat(domain.getCreatedAt()).isNull();
    }
}
