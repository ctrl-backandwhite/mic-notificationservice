package com.backandwhite.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.model.NotificationType;
import com.backandwhite.domain.port.NotificationSender;
import com.backandwhite.domain.repository.NotificationRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailRetryJobTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private EmailRetryJob emailRetryJob;

    private Notification failedNotification(Long id, int retryCount) {
        return Notification.builder().id(id).recipient("user@example.com").subject("Test").type(NotificationType.EMAIL)
                .status(NotificationStatus.FAILED).retryCount(retryCount).build();
    }

    @Test
    void retryFailedEmails_noFailedNotifications_doesNothing() {
        when(notificationRepository.findByStatusAndRetryCountLessThan(any(), anyInt()))
                .thenReturn(Collections.emptyList());

        emailRetryJob.retryFailedEmails();

        verify(notificationSender, never()).send(any());
        verify(notificationRepository, never()).update(any());
    }

    @Test
    void retryFailedEmails_withFailedNotifications_retriesEach() {
        Notification n1 = failedNotification(1L, 1);
        Notification n2 = failedNotification(2L, 0);
        when(notificationRepository.findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, 3))
                .thenReturn(List.of(n1, n2));

        emailRetryJob.retryFailedEmails();

        assertThat(n1.getStatus()).isEqualTo(NotificationStatus.RETRYING);
        assertThat(n2.getStatus()).isEqualTo(NotificationStatus.RETRYING);
        verify(notificationRepository, times(2)).update(any());
        verify(notificationSender, times(2)).send(any());
    }

    @Test
    void retryFailedEmails_setsStatusToRetryingAndClearsError() {
        Notification n1 = failedNotification(1L, 1);
        n1.setErrorMessage("Previous error");
        when(notificationRepository.findByStatusAndRetryCountLessThan(any(), anyInt())).thenReturn(List.of(n1));

        emailRetryJob.retryFailedEmails();

        assertThat(n1.getStatus()).isEqualTo(NotificationStatus.RETRYING);
        assertThat(n1.getErrorMessage()).isNull();
    }

    @Test
    void retryFailedEmails_emailServiceThrows_catchesAndContinues() {
        Notification n1 = failedNotification(1L, 1);
        Notification n2 = failedNotification(2L, 0);
        when(notificationRepository.findByStatusAndRetryCountLessThan(any(), anyInt())).thenReturn(List.of(n1, n2));
        doThrow(new RuntimeException("SMTP error")).when(notificationSender).send(n1);

        emailRetryJob.retryFailedEmails();

        // n1 failed but n2 should still be processed
        verify(notificationSender).send(n1);
        verify(notificationSender).send(n2);
    }
}
