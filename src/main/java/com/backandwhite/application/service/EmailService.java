package com.backandwhite.application.service;

import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Log4j2
@Service
@AllArgsConstructor
public class EmailService {

    private static final Locale DEFAULT_EMAIL_LOCALE = Locale.forLanguageTag("es");

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationRepository notificationRepository;

    public void sendEmail(Notification notification) {
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
            throw new RuntimeException("Error sending email for notification id=" + notification.getId(), e);
        }
    }

    private String buildEmailContent(Notification notification) {
        Locale locale = DEFAULT_EMAIL_LOCALE;
        Map<String, Object> variables = notification.getVariables();
        if (variables != null) {
            Object lang = variables.get("lang");
            if (lang != null) {
                String langTag = lang.toString();
                if (!langTag.isBlank()) {
                    locale = Locale.forLanguageTag(langTag);
                }
            }
        }

        Context context = new Context(locale);
        if (variables != null) {
            variables.forEach(context::setVariable);
        }

        String templateFile = notification.getTemplate() != null
                ? notification.getTemplate().getTemplateFile()
                : "email/default";

        return templateEngine.process(templateFile, context);
    }
}
