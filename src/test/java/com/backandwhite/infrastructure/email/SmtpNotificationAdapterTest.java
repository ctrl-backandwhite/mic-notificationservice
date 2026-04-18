package com.backandwhite.infrastructure.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.domain.model.NotificationType;
import com.backandwhite.domain.repository.NotificationRepository;
import com.backandwhite.infrastructure.email.mapper.EmailContextMapper;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

@ExtendWith(MockitoExtension.class)
class SmtpNotificationAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MimeMessage mimeMessage;

    private final EmailContextMapper emailContextMapper = Mappers.getMapper(EmailContextMapper.class);

    @InjectMocks
    private SmtpNotificationAdapter smtpNotificationAdapter;

    SmtpNotificationAdapterTest() {
        // nothing
    }

    private SmtpNotificationAdapter newAdapter() {
        return new SmtpNotificationAdapter(mailSender, templateEngine, notificationRepository, emailContextMapper);
    }

    private Notification baseNotification() {
        return Notification.builder().id(1L).recipient("test@example.com").subject("Test Subject")
                .type(NotificationType.EMAIL).status(NotificationStatus.PENDING)
                .template(NotificationTemplate.builder().templateFile("email/welcome").build())
                .variables(Map.of("name", "User")).retryCount(0).build();
    }

    @Test
    void send_success_setsStatusToSent() {
        SmtpNotificationAdapter adapter = newAdapter();
        Notification notification = baseNotification();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/welcome"), any(IContext.class))).thenReturn("<html>hello</html>");

        adapter.send(notification);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getSentAt()).isNotNull();
        verify(mailSender).send(mimeMessage);
        verify(notificationRepository).update(notification);
    }

    @Test
    void send_noTemplate_usesDefaultTemplateFile() {
        SmtpNotificationAdapter adapter = newAdapter();
        Notification notification = baseNotification().withTemplate(null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/default"), any(IContext.class))).thenReturn("<html>default</html>");

        adapter.send(notification);

        verify(templateEngine).process(eq("email/default"), any(IContext.class));
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void send_withLangVariable_usesLocale() {
        SmtpNotificationAdapter adapter = newAdapter();
        Notification notification = baseNotification().withVariables(Map.of("lang", "en", "name", "User"));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/welcome"), any(IContext.class))).thenReturn("<html>hello</html>");

        adapter.send(notification);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void send_withBlankLangVariable_usesDefaultLocale() {
        SmtpNotificationAdapter adapter = newAdapter();
        Notification notification = baseNotification().withVariables(Map.of("lang", "  ", "name", "User"));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/welcome"), any(IContext.class))).thenReturn("<html>hello</html>");

        adapter.send(notification);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void send_withNullVariables_doesNotFail() {
        SmtpNotificationAdapter adapter = newAdapter();
        Notification notification = baseNotification().withVariables(null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/welcome"), any(IContext.class))).thenReturn("<html>hello</html>");

        adapter.send(notification);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void send_failure_setsStatusToFailedAndThrows() {
        SmtpNotificationAdapter adapter = newAdapter();
        Notification notification = baseNotification();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn("<html>hello</html>");
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> adapter.send(notification)).isInstanceOf(MailSendException.class);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getErrorMessage()).contains("SMTP error");
        assertThat(notification.getRetryCount()).isEqualTo(1);
        verify(notificationRepository).update(notification);
    }

    @Test
    void send_failureWithNullRetryCount_setsRetryCountToOne() {
        SmtpNotificationAdapter adapter = newAdapter();
        Notification notification = baseNotification().withRetryCount(null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn("<html>hello</html>");
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> adapter.send(notification)).isInstanceOf(MailSendException.class);

        assertThat(notification.getRetryCount()).isEqualTo(1);
    }

    @Test
    void send_failureWithExistingRetryCount_incrementsRetryCount() {
        SmtpNotificationAdapter adapter = newAdapter();
        Notification notification = baseNotification().withRetryCount(2);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn("<html>hello</html>");
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> adapter.send(notification)).isInstanceOf(MailSendException.class);

        assertThat(notification.getRetryCount()).isEqualTo(3);
    }

    @Test
    void send_failureWithNullMessage_setsUnknownError() {
        SmtpNotificationAdapter adapter = newAdapter();
        Notification notification = baseNotification();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn("<html>hello</html>");
        doThrow(new RuntimeException((String) null)).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> adapter.send(notification)).isInstanceOf(MailSendException.class);

        assertThat(notification.getErrorMessage()).isEqualTo("Unknown error");
    }
}
