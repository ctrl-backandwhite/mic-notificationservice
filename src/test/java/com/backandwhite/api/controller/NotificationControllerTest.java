package com.backandwhite.api.controller;

import com.backandwhite.api.dto.in.NotificationDtoIn;
import com.backandwhite.api.dto.out.NotificationDtoOut;
import com.backandwhite.api.mapper.NotificationDtoMapper;
import com.backandwhite.application.service.EmailService;
import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.application.usecase.NotificationUseCase;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.provider.NotificationProvider;
import com.backandwhite.provider.NotificationTemplateProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationDtoMapper mapper;
    @Mock
    private NotificationUseCase useCase;
    @Mock
    private NotificationTemplateUseCase templateUseCase;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationController controller;

    @Test
    void create_withTemplate_savesAndSendsEmail() {
        NotificationDtoIn dtoIn = NotificationProvider.notificationDtoIn();
        Notification domain = NotificationProvider.notification();
        Notification saved = NotificationProvider.notification();
        NotificationDtoOut dtoOut = NotificationProvider.notificationDtoOut(NotificationProvider.NOTIFICATION_ID);

        when(mapper.toDomain(dtoIn)).thenReturn(domain);
        when(templateUseCase.getById(dtoIn.getTemplateId()))
                .thenReturn(NotificationTemplateProvider.template());
        when(useCase.save(any())).thenReturn(saved);
        when(mapper.toDtoOut(saved)).thenReturn(dtoOut);

        ResponseEntity<NotificationDtoOut> response = controller.create(dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(emailService).sendEmail(saved);
    }

    @Test
    void create_withoutTemplate_skipsTemplateLoad() {
        NotificationDtoIn dtoIn = NotificationProvider.notificationDtoIn().withTemplateId(null);
        Notification domain = NotificationProvider.notification();
        Notification saved = NotificationProvider.notification();
        NotificationDtoOut dtoOut = NotificationProvider.notificationDtoOut(NotificationProvider.NOTIFICATION_ID);

        when(mapper.toDomain(dtoIn)).thenReturn(domain);
        when(useCase.save(any())).thenReturn(saved);
        when(mapper.toDtoOut(saved)).thenReturn(dtoOut);

        ResponseEntity<NotificationDtoOut> response = controller.create(dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verifyNoInteractions(templateUseCase);
    }

    @Test
    void getById_returnsNotification() {
        Notification domain = NotificationProvider.notification();
        NotificationDtoOut dtoOut = NotificationProvider.notificationDtoOut(NotificationProvider.NOTIFICATION_ID);

        when(useCase.getById(NotificationProvider.NOTIFICATION_ID)).thenReturn(domain);
        when(mapper.toDtoOut(domain)).thenReturn(dtoOut);

        ResponseEntity<NotificationDtoOut> response = controller.getById(NotificationProvider.NOTIFICATION_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
    }

    @Test
    void findAll_returnsList() {
        List<Notification> notifications = List.of(NotificationProvider.notification());
        List<NotificationDtoOut> dtoOuts = List.of(
                NotificationProvider.notificationDtoOut(NotificationProvider.NOTIFICATION_ID));

        when(useCase.findAll()).thenReturn(notifications);
        when(mapper.toDtoOutList(notifications)).thenReturn(dtoOuts);

        ResponseEntity<List<NotificationDtoOut>> response = controller.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = controller.delete(NotificationProvider.NOTIFICATION_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(useCase).delete(NotificationProvider.NOTIFICATION_ID);
    }

    @Test
    void findByStatus_returnsMatchingNotifications() {
        List<Notification> sent = List.of(NotificationProvider.notification());
        List<NotificationDtoOut> dtoOuts = List.of(
                NotificationProvider.notificationDtoOut(NotificationProvider.NOTIFICATION_ID));

        when(useCase.findByStatus(NotificationStatus.SENT)).thenReturn(sent);
        when(mapper.toDtoOutList(sent)).thenReturn(dtoOuts);

        ResponseEntity<List<NotificationDtoOut>> response = controller.findByStatus(NotificationStatus.SENT);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }
}
