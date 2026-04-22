package com.backandwhite.application.service;

import com.backandwhite.common.constants.AppConstants;
import java.util.Map;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Consults the user's {@code UserNotificationPref} at mic-userdetailservice
 * before the notification layer dispatches an email, honouring the toggles the
 * customer set in /account → Security. Transactional templates (order-*,
 * payment-*, refund-*, invoice-*, password-*, session-*) are always allowed — a
 * customer can't opt out of receipts or security alerts. Marketing categories
 * (newsletter, promo, flash) check {@code emailPromos}.
 *
 * <p>
 * Fails open on network / 404 / 5xx: if userdetailservice is down we still
 * send. The alternative (blocking receipts when a service is degraded) is worse
 * UX. Errors are logged for observability.
 */
@Log4j2
@Service
public class NotificationPreferencesChecker {

    /**
     * Template names that count as marketing — the checker consults the user's
     * {@code emailPromos} toggle for these. Anything not listed is treated as
     * transactional and always sent.
     */
    private static final Set<String> MARKETING_TEMPLATES = Set.of("newsletter-welcome", "newsletter-campaign", "promo",
            "promo-flash", "campaign-announcement", "loyalty-promo", "abandoned-cart");

    @Value("${app.userdetail.url:http://localhost:6008}")
    private String userDetailUrl;

    private final RestClient restClient = RestClient.builder().build();

    /**
     * @return {@code true} if the notification should be dispatched, {@code false}
     *         only when the user explicitly opted out of this category.
     */
    public boolean shouldSend(String recipientEmail, String templateName) {
        if (recipientEmail == null || recipientEmail.isBlank())
            return false;
        if (!isMarketing(templateName))
            return true;

        try {
            Map<?, ?> body = restClient.get()
                    .uri(userDetailUrl + "/api/v1/notification-prefs/by-email/{email}", recipientEmail)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer internal")
                    .header(AppConstants.HEADER_NX036_AUTH, "notification-service").retrieve().body(Map.class);
            if (body == null)
                return true;
            Object promos = body.get("emailPromos");
            // Missing or null → default on: we keep the historical behaviour
            // of sending until the user explicitly turns marketing off.
            return !(promos instanceof Boolean b && !b);
        } catch (Exception e) {
            log.warn("::> prefs lookup failed for {} — falling back to send: {}", recipientEmail, e.getMessage());
            return true;
        }
    }

    private boolean isMarketing(String templateName) {
        if (templateName == null)
            return false;
        if (MARKETING_TEMPLATES.contains(templateName))
            return true;
        return templateName.startsWith("newsletter") || templateName.startsWith("promo")
                || templateName.startsWith("campaign");
    }
}
