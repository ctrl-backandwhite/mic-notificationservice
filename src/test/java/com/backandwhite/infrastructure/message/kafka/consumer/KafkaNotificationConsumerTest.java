package com.backandwhite.infrastructure.message.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.backandwhite.application.handler.NotificationCommandHandler;
import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.core.kafka.avro.EmailNotificationEvent;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.domain.model.NotificationType;
import com.backandwhite.domain.port.NotificationSender;
import com.backandwhite.domain.repository.NotificationRepository;
import com.backandwhite.infrastructure.message.kafka.mapper.NotificationEventMapper;
import com.backandwhite.infrastructure.message.kafka.mapper.NotificationEventMapperImpl;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaNotificationConsumerTest {

    @Mock
    private NotificationSender notificationSender;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationTemplateUseCase notificationTemplateUseCase;

    @Mock
    private NotificationCommandHandler notificationCommandHandler;

    @Spy
    private NotificationEventMapper notificationEventMapper = new NotificationEventMapperImpl();

    @InjectMocks
    private KafkaNotificationConsumer consumer;

    private EmailNotificationEvent buildEvent(String recipient, String subject, String templateName,
            Map<String, String> variables) {
        EmailNotificationEvent event = new EmailNotificationEvent();
        event.setRecipient(recipient);
        event.setSubject(subject);
        event.setTemplateName(templateName);
        event.setVariables(variables);
        return event;
    }

    @Test
    void consume_validEvent_savesAndSendsEmail() {
        EmailNotificationEvent event = buildEvent("user@test.com", "Welcome", "welcome-email", Map.of("name", "User"));
        NotificationTemplate template = NotificationTemplate.builder().name("welcome-email")
                .templateFile("email/welcome").active(true).subject("Welcome!").build();
        when(notificationTemplateUseCase.findByName("welcome-email")).thenReturn(Optional.of(template));
        Notification saved = Notification.builder().id(1L).recipient("user@test.com").build();
        when(notificationRepository.save(any())).thenReturn(saved);

        consumer.consume(event);

        verify(notificationCommandHandler).validate(any());
        verify(notificationRepository).save(any());
        verify(notificationSender).send(saved);
    }

    @Test
    void consume_withActiveTemplate_setsTemplateOnNotification() {
        EmailNotificationEvent event = buildEvent("user@test.com", null, "welcome-email", null);
        NotificationTemplate template = NotificationTemplate.builder().name("welcome-email")
                .templateFile("email/welcome").active(true).subject("Default Subject").build();
        when(notificationTemplateUseCase.findByName("welcome-email")).thenReturn(Optional.of(template));
        Notification saved = Notification.builder().id(1L).build();
        when(notificationRepository.save(any())).thenReturn(saved);

        consumer.consume(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification captured = captor.getValue();
        assertThat(captured.getTemplate()).isEqualTo(template);
        assertThat(captured.getSubject()).isEqualTo("Default Subject");
    }

    @Test
    void consume_templateNotFound_fallsBackToFileTemplate() {
        EmailNotificationEvent event = buildEvent("user@test.com", "Subject", "nonexistent", null);
        when(notificationTemplateUseCase.findByName("nonexistent")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            // At save time, file-based template must NOT be set (avoids
            // TransientPropertyValueException)
            assertThat(n.getTemplate()).isNull();
            return n;
        });

        consumer.consume(event);

        // After the flow, the file-based template is set for email rendering
        ArgumentCaptor<Notification> sendCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationSender).send(sendCaptor.capture());
        NotificationTemplate fallback = sendCaptor.getValue().getTemplate();
        assertThat(fallback).isNotNull();
        assertThat(fallback.getName()).isEqualTo("nonexistent");
        assertThat(fallback.getTemplateFile()).isEqualTo("email/nonexistent");
        assertThat(fallback.getActive()).isTrue();
    }

    @Test
    void consume_inactiveTemplate_proceedsWithoutTemplate() {
        EmailNotificationEvent event = buildEvent("user@test.com", "Subject", "inactive-template", null);
        NotificationTemplate template = NotificationTemplate.builder().name("inactive-template").active(false).build();
        when(notificationTemplateUseCase.findByName("inactive-template")).thenReturn(Optional.of(template));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getTemplate()).isNull();
    }

    @Test
    void consume_nullTemplateName_skipsTemplateResolution() {
        EmailNotificationEvent event = buildEvent("user@test.com", "Subject", null, null);
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(event);

        verify(notificationTemplateUseCase, never()).findByName(any());
        verify(notificationRepository).save(any());
        verify(notificationSender).send(any());
    }

    @Test
    void consume_withExistingSubject_doesNotOverrideWithTemplateSubject() {
        EmailNotificationEvent event = buildEvent("user@test.com", "My Subject", "welcome-email", null);
        NotificationTemplate template = NotificationTemplate.builder().name("welcome-email")
                .templateFile("email/welcome").active(true).subject("Template Subject").build();
        when(notificationTemplateUseCase.findByName("welcome-email")).thenReturn(Optional.of(template));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getSubject()).isEqualTo("My Subject");
    }

    @Test
    void consumeContactMessage_validEvent_processesCorrectly() {
        EmailNotificationEvent event = buildEvent("contact@test.com", "Contact Form", "contact-message",
                Map.of("message", "Hello"));
        NotificationTemplate template = NotificationTemplate.builder().name("contact-message")
                .templateFile("email/contact").active(true).subject("Contact").build();
        when(notificationTemplateUseCase.findByName("contact-message")).thenReturn(Optional.of(template));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consumeContactMessage(event);

        verify(notificationCommandHandler).validate(any());
        verify(notificationRepository).save(any());
        verify(notificationSender).send(any());
    }

    @Test
    void consume_withVariables_copiesVariablesToNotification() {
        EmailNotificationEvent event = buildEvent("user@test.com", "Subject", null, Map.of("key1", "value1"));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getVariables()).containsEntry("key1", "value1");
    }

    @Test
    void consume_setsCorrectDefaults() {
        EmailNotificationEvent event = buildEvent("user@test.com", "Subject", null, null);
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        consumer.consume(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification n = captor.getValue();
        assertThat(n.getType()).isEqualTo(NotificationType.EMAIL);
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(n.getRetryCount()).isZero();
    }
}
