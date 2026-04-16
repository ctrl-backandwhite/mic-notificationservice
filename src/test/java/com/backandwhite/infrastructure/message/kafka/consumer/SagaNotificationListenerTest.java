package com.backandwhite.infrastructure.message.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.core.kafka.avro.SagaNotifyFailureEvent;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.domain.model.NotificationType;
import com.backandwhite.domain.port.NotificationSender;
import com.backandwhite.domain.repository.NotificationRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SagaNotificationListenerTest {

    @Mock
    private NotificationSender notificationSender;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationTemplateUseCase notificationTemplateUseCase;

    @InjectMocks
    private SagaNotificationListener listener;

    private SagaNotifyFailureEvent buildEvent() {
        return SagaNotifyFailureEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1").setEmail("user@test.com")
                .setOrderReference("REF-001").setAmount("99.99").setCurrency("EUR").setReason("Insufficient funds")
                .setTimestamp("2025-01-01T00:00:00Z").build();
    }

    @Test
    void onNotifyFailure_templateFound_usesDbTemplate() {
        NotificationTemplate template = NotificationTemplate.builder().name("order-payment-failed")
                .templateFile("email/order-payment-failed").active(true).subject("Payment Failed").build();
        when(notificationTemplateUseCase.findByName("order-payment-failed")).thenReturn(Optional.of(template));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onNotifyFailure(buildEvent());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getTemplate()).isEqualTo(template);
        verify(notificationSender).send(any());
    }

    @Test
    void onNotifyFailure_templateNotFound_usesFallback() {
        when(notificationTemplateUseCase.findByName("order-payment-failed")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onNotifyFailure(buildEvent());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        NotificationTemplate fallback = captor.getValue().getTemplate();
        assertThat(fallback.getName()).isEqualTo("order-payment-failed");
        assertThat(fallback.getTemplateFile()).isEqualTo("email/order-payment-failed");
        assertThat(fallback.getActive()).isTrue();
    }

    @Test
    void onNotifyFailure_inactiveTemplate_usesFallback() {
        NotificationTemplate template = NotificationTemplate.builder().name("order-payment-failed")
                .templateFile("email/order-payment-failed").active(false).build();
        when(notificationTemplateUseCase.findByName("order-payment-failed")).thenReturn(Optional.of(template));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onNotifyFailure(buildEvent());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        NotificationTemplate fallback = captor.getValue().getTemplate();
        assertThat(fallback.getTemplateFile()).isEqualTo("email/order-payment-failed");
        assertThat(fallback.getActive()).isTrue();
    }

    @Test
    void onNotifyFailure_setsCorrectNotificationFields() {
        when(notificationTemplateUseCase.findByName("order-payment-failed")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onNotifyFailure(buildEvent());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification n = captor.getValue();
        assertThat(n.getRecipient()).isEqualTo("user@test.com");
        assertThat(n.getSubject()).contains("REF-001");
        assertThat(n.getType()).isEqualTo(NotificationType.EMAIL);
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(n.getRetryCount()).isZero();
    }

    @Test
    void onNotifyFailure_capturesVariables() {
        when(notificationTemplateUseCase.findByName("order-payment-failed")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onNotifyFailure(buildEvent());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getVariables()).containsEntry("orderId", "ORD-1")
                .containsEntry("orderReference", "REF-001").containsEntry("amount", "99.99")
                .containsEntry("currency", "EUR").containsEntry("reason", "Insufficient funds");
    }

    @Test
    void onNotifyFailure_emailServiceThrows_catchesException() {
        when(notificationTemplateUseCase.findByName("order-payment-failed")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("SMTP error")).when(notificationSender).send(any());

        // Should NOT throw
        listener.onNotifyFailure(buildEvent());

        verify(notificationSender).send(any());
    }
}
