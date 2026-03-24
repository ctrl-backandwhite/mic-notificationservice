package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.handler.NotificationCommandHandler;
import com.backandwhite.common.exception.EntityNotFoundException;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.repository.NotificationRepository;
import com.backandwhite.provider.NotificationProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.backandwhite.provider.NotificationProvider.failedNotification;
import static com.backandwhite.provider.NotificationProvider.notification;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationUseCaseImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationCommandHandler notificationCommandHandler;

    @InjectMocks
    private NotificationUseCaseImpl notificationUseCase;

    @Test
    void save_validNotification_delegatesToRepository() {
        Notification input = notification().withId(null);
        Notification saved = notification();

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        Notification result = notificationUseCase.save(input);

        assertSame(saved, result);
        verify(notificationCommandHandler).validate(input);
        verify(notificationRepository).save(input);
    }

    @Test
    void findAll_returnsRepositoryList() {
        List<Notification> notifications = List.of(notification(), failedNotification());

        when(notificationRepository.findAll()).thenReturn(notifications);

        List<Notification> result = notificationUseCase.findAll();

        assertSame(notifications, result);
        verify(notificationRepository).findAll();
    }

    @Test
    void getById_existingNotification_returnsNotification() {
        Notification model = notification();

        when(notificationRepository.getById(NotificationProvider.NOTIFICATION_ID)).thenReturn(model);

        Notification result = notificationUseCase.getById(NotificationProvider.NOTIFICATION_ID);

        assertSame(model, result);
        verify(notificationRepository).getById(NotificationProvider.NOTIFICATION_ID);
    }

    @Test
    void getById_missingNotification_throwsEntityNotFound() {
        when(notificationRepository.getById(99L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> notificationUseCase.getById(99L));
        verify(notificationRepository).getById(99L);
    }

    @Test
    void delete_existingNotification_delegatesToRepository() {
        when(notificationRepository.getById(NotificationProvider.NOTIFICATION_ID)).thenReturn(notification());

        notificationUseCase.delete(NotificationProvider.NOTIFICATION_ID);

        verify(notificationRepository).delete(NotificationProvider.NOTIFICATION_ID);
    }

    @Test
    void findByStatus_returnsMatchingNotifications() {
        List<Notification> sent = List.of(notification());

        when(notificationRepository.findByStatus(NotificationStatus.SENT)).thenReturn(sent);

        List<Notification> result = notificationUseCase.findByStatus(NotificationStatus.SENT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void findByRecipient_returnsMatchingNotifications() {
        List<Notification> forRecipient = List.of(notification());

        when(notificationRepository.findByRecipient(NotificationProvider.RECIPIENT)).thenReturn(forRecipient);

        List<Notification> result = notificationUseCase.findByRecipient(NotificationProvider.RECIPIENT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecipient()).isEqualTo(NotificationProvider.RECIPIENT);
    }
}
