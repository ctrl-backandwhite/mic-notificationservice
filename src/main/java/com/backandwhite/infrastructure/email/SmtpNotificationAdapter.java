package com.backandwhite.infrastructure.email;

import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.port.NotificationSender;
import com.backandwhite.domain.repository.NotificationRepository;
import com.backandwhite.infrastructure.email.mapper.EmailContextMapper;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * SMTP adapter implementing the {@link NotificationSender} domain port. Builds
 * the Thymeleaf email context via a MapStruct-backed mapper
 * ({@link EmailContextMapper}) and delegates to {@link JavaMailSender}.
 */
@Log4j2
@Service
@AllArgsConstructor
public class SmtpNotificationAdapter implements NotificationSender {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationRepository notificationRepository;
    private final EmailContextMapper emailContextMapper;

    @Override
    public void send(Notification notification) {
        try {
            String htmlContent = buildEmailContent(notification);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notification.getRecipient());
            helper.setSubject(notification.getSubject());
            helper.setText(htmlContent, true);

            mailSender.send(message);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notificationRepository.update(notification);

            log.debug("::> Email sent successfully for notification id={}", notification.getId());
        } catch (Exception e) {
            log.error("::> Error sending email for notification id={}: {}", notification.getId(), e.getMessage(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage() != null
                    ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 1000))
                    : "Unknown error");
            notification.setRetryCount(notification.getRetryCount() == null ? 1 : notification.getRetryCount() + 1);
            notificationRepository.update(notification);
            throw new MailSendException("Error sending email for notification id=" + notification.getId(), e);
        }
    }

    private String buildEmailContent(Notification notification) {
        Context context = emailContextMapper.toContext(notification);
        String templateFile = emailContextMapper.resolveTemplateFile(notification);
        return templateEngine.process(templateFile, context);
    }
}
