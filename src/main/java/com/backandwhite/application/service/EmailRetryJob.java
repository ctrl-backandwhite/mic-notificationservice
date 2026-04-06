package com.backandwhite.application.service;

import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class EmailRetryJob {

    private static final int DEFAULT_MAX_RETRIES = 3;

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Value("${app.email.retry.max-retries:3}")
    private int maxRetries = DEFAULT_MAX_RETRIES;

    /**
     * Runs every 5 minutes to retry FAILED email notifications
     * that have not exceeded the maximum retry count.
     */
    @Scheduled(fixedDelayString = "${app.email.retry.interval-ms:300000}", initialDelay = 60000)
    public void retryFailedEmails() {
        List<Notification> failedNotifications = notificationRepository
                .findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, maxRetries);

        if (failedNotifications.isEmpty()) {
            return;
        }

        log.info("::> Email retry job: found {} failed notifications to retry", failedNotifications.size());

        for (Notification notification : failedNotifications) {
            try {
                notification.setStatus(NotificationStatus.RETRYING);
                notification.setErrorMessage(null);
                notificationRepository.update(notification);

                emailService.sendEmail(notification);
                log.info("::> Email retry successful for notification id={} to {}",
                        notification.getId(), notification.getRecipient());
            } catch (Exception e) {
                log.warn("::> Email retry failed for notification id={} to {} (attempt {}/{}): {}",
                        notification.getId(),
                        notification.getRecipient(),
                        notification.getRetryCount(),
                        maxRetries,
                        e.getMessage());
            }
        }
    }
}
