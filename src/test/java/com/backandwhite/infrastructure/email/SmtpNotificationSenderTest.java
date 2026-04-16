package com.backandwhite.infrastructure.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.backandwhite.domain.model.*;
import com.backandwhite.domain.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

@ExtendWith(MockitoExtension.class)
class SmtpNotificationSenderTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private SmtpNotificationSender smtpNotificationSender;

    private Notification baseNotification() {
        return Notification.builder().id(1L).recipient("test@example.com").subject("Test Subject")
                .type(NotificationType.EMAIL).status(NotificationStatus.PENDING)
                .template(NotificationTemplate.builder().templateFile("email/welcome").build())
                .variables(Map.of("name", "User")).retryCount(0).build();
    }

    @Test
    void send_success_setsStatusToSent() {
        Notification notification = baseNotification();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/welcome"), any())).thenReturn("<html>hello</html>");

        smtpNotificationSender.send(notification);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getSentAt()).isNotNull();
        verify(mailSender).send(mimeMessage);
        verify(notificationRepository).update(notification);
    }

    @Test
    void send_noTemplate_usesDefaultTemplateFile() {
        Notification notification = baseNotification().withTemplate(null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/default"), any())).thenReturn("<html>default</html>");

        smtpNotificationSender.send(notification);

        verify(templateEngine).process(eq("email/default"), any());
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void send_withLangVariable_usesLocale() {
        Notification notification = baseNotification().withVariables(Map.of("lang", "en", "name", "User"));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/welcome"), any())).thenReturn("<html>hello</html>");

        smtpNotificationSender.send(notification);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void send_withBlankLangVariable_usesDefaultLocale() {
        Notification notification = baseNotification().withVariables(Map.of("lang", "  ", "name", "User"));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/welcome"), any())).thenReturn("<html>hello</html>");

        smtpNotificationSender.send(notification);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void send_withNullVariables_doesNotFail() {
        Notification notification = baseNotification().withVariables(null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/welcome"), any())).thenReturn("<html>hello</html>");

        smtpNotificationSender.send(notification);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void send_failure_setsStatusToFailedAndThrows() {
        Notification notification = baseNotification();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn("<html>hello</html>");
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> smtpNotificationSender.send(notification)).isInstanceOf(MailSendException.class);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getErrorMessage()).contains("SMTP error");
        assertThat(notification.getRetryCount()).isEqualTo(1);
        verify(notificationRepository).update(notification);
    }

    @Test
    void send_failureWithNullRetryCount_setsRetryCountToOne() {
        Notification notification = baseNotification().withRetryCount(null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn("<html>hello</html>");
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> smtpNotificationSender.send(notification)).isInstanceOf(MailSendException.class);

        assertThat(notification.getRetryCount()).isEqualTo(1);
    }

    @Test
    void send_failureWithExistingRetryCount_incrementsRetryCount() {
        Notification notification = baseNotification().withRetryCount(2);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn("<html>hello</html>");
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> smtpNotificationSender.send(notification)).isInstanceOf(MailSendException.class);

        assertThat(notification.getRetryCount()).isEqualTo(3);
    }

    @Test
    void send_failureWithNullMessage_setsUnknownError() {
        Notification notification = baseNotification();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn("<html>hello</html>");
        doThrow(new RuntimeException((String) null)).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> smtpNotificationSender.send(notification)).isInstanceOf(MailSendException.class);

        assertThat(notification.getErrorMessage()).isEqualTo("Unknown error");
    }
}
