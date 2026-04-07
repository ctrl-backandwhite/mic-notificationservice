package com.backandwhite.infrastructure.message.kafka.consumer;

import com.backandwhite.application.handler.NotificationCommandHandler;
import com.backandwhite.application.service.EmailService;
import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.core.kafka.avro.*;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationStatus;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.domain.model.NotificationType;
import com.backandwhite.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Consumes business domain events and triggers email notifications.
 * Follows the same pattern as KafkaNotificationConsumer:
 * build Notification → validate → resolve template → persist → send email.
 */
@Log4j2
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.listener.auto-startup", havingValue = "true", matchIfMissing = true)
public class BusinessEventNotificationConsumer {

    private final EmailService emailService;
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateUseCase notificationTemplateUseCase;
    private final NotificationCommandHandler notificationCommandHandler;

    @Value("${app.store.url:http://localhost:9000}")
    private String storeUrl;

    @Value("${app.store.giftcard-activate-path:/cuenta?tab=giftcards}")
    private String giftcardActivatePath;

    // ─── Order Events ───────────────────────────────────────────────

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_ORDER_CREATED, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void onOrderCreated(OrderCreatedEvent event) {
        String email = str(event.getEmail());
        if (email == null) {
            log.warn("::> order.created without email, skipping notification. orderId={}", str(event.getOrderId()));
            return;
        }
        log.info("::> Notification: order.created orderId={}, email={}", str(event.getOrderId()), email);

        Map<String, Object> vars = new HashMap<>();
        vars.put("orderId", str(event.getOrderId()));
        vars.put("orderReference", str(event.getOrderReference()));
        vars.put("totalAmount", str(event.getTotalAmount()));
        vars.put("currency", str(event.getCurrency()));
        vars.put("itemCount", String.valueOf(event.getItemCount()));
        vars.put("status", str(event.getStatus()));

        sendNotification(email, "Order Confirmation", "order-confirmation", vars);
    }

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_ORDER_CONFIRMED, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        String email = str(event.getEmail());
        if (email == null)
            return;
        log.info("::> Notification: order.confirmed orderId={}", str(event.getOrderId()));

        Map<String, Object> vars = new HashMap<>();
        vars.put("orderId", str(event.getOrderId()));
        vars.put("orderReference", str(event.getOrderReference()));
        vars.put("totalAmount", str(event.getTotalAmount()));
        vars.put("currency", str(event.getCurrency()));

        sendNotification(email, "Order Confirmed", "order-confirmed", vars);
    }

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_ORDER_CANCELLED, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void onOrderCancelled(OrderCancelledEvent event) {
        String email = str(event.getEmail());
        if (email == null)
            return;
        log.info("::> Notification: order.cancelled orderId={}", str(event.getOrderId()));

        Map<String, Object> vars = new HashMap<>();
        vars.put("orderId", str(event.getOrderId()));
        vars.put("orderReference", str(event.getOrderReference()));
        vars.put("reason", str(event.getReason()));

        sendNotification(email, "Order Cancelled", "order-cancelled", vars);
    }

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_ORDER_SHIPPED, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void onOrderShipped(OrderShippedEvent event) {
        String email = str(event.getEmail());
        if (email == null)
            return;
        log.info("::> Notification: order.shipped orderId={}", str(event.getOrderId()));

        Map<String, Object> vars = new HashMap<>();
        vars.put("orderId", str(event.getOrderId()));
        vars.put("orderReference", str(event.getOrderReference()));
        vars.put("trackingNumber", str(event.getTrackingNumber()));
        vars.put("carrier", str(event.getCarrier()));
        vars.put("estimatedDelivery", str(event.getEstimatedDelivery()));

        sendNotification(email, "Your Order Has Been Shipped", "order-shipped", vars);
    }

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_ORDER_DELIVERED, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void onOrderDelivered(OrderDeliveredEvent event) {
        String email = str(event.getEmail());
        if (email == null)
            return;
        log.info("::> Notification: order.delivered orderId={}", str(event.getOrderId()));

        Map<String, Object> vars = new HashMap<>();
        vars.put("orderId", str(event.getOrderId()));
        vars.put("orderReference", str(event.getOrderReference()));
        vars.put("totalAmount", str(event.getTotalAmount()));

        sendNotification(email, "Your Order Has Been Delivered", "order-delivered", vars);
    }

    // ─── Payment Events ─────────────────────────────────────────────

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_PAYMENT_CONFIRMED, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void onPaymentConfirmed(PaymentConfirmedEvent event) {
        String email = str(event.getEmail());
        if (email == null)
            return;
        log.info("::> Notification: payment.confirmed paymentId={}", str(event.getPaymentId()));

        Map<String, Object> vars = new HashMap<>();
        vars.put("paymentId", str(event.getPaymentId()));
        vars.put("orderId", str(event.getOrderId()));
        vars.put("amount", str(event.getAmount()));
        vars.put("currency", str(event.getCurrency()));
        vars.put("method", str(event.getMethod()));

        sendNotification(email, "Payment Confirmed", "payment-confirmed", vars);
    }

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_PAYMENT_FAILED, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void onPaymentFailed(PaymentFailedEvent event) {
        String email = str(event.getEmail());
        if (email == null)
            return;
        log.info("::> Notification: payment.failed paymentId={}", str(event.getPaymentId()));

        Map<String, Object> vars = new HashMap<>();
        vars.put("paymentId", str(event.getPaymentId()));
        vars.put("orderId", str(event.getOrderId()));
        vars.put("amount", str(event.getAmount()));
        vars.put("reason", str(event.getReason()));

        sendNotification(email, "Payment Failed", "payment-failed", vars);
    }

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_REFUND_COMPLETED, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void onRefundCompleted(PaymentRefundCompletedEvent event) {
        String email = str(event.getEmail());
        if (email == null)
            return;
        log.info("::> Notification: refund.completed refundId={}", str(event.getRefundId()));

        Map<String, Object> vars = new HashMap<>();
        vars.put("refundId", str(event.getRefundId()));
        vars.put("orderId", str(event.getOrderId()));
        vars.put("refundAmount", str(event.getRefundAmount()));

        sendNotification(email, "Refund Processed", "refund-completed", vars);
    }

    // ─── Newsletter Events ──────────────────────────────────────────

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_CUSTOMER_NEWSLETTER_SUBSCRIBED, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void onNewsletterSubscribed(NewsletterSubscribedEvent event) {
        String email = str(event.getEmail());
        if (email == null)
            return;
        log.info("::> Notification: newsletter.subscribed email={}", email);

        Map<String, Object> vars = new HashMap<>();
        vars.put("source", str(event.getSource()));

        sendNotification(email, "Welcome to Our Newsletter", "newsletter-welcome", vars);
    }

    // ─── Stock Alert (Admin Notification) ───────────────────────────

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_STOCK_LOW_ALERT, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void onStockLowAlert(StockLowAlertEvent event) {
        log.warn("::> STOCK LOW ALERT: productId={}, variantId={}, stock={}, threshold={}",
                str(event.getProductId()), str(event.getVariantId()),
                event.getCurrentStock(), event.getThreshold());

        Map<String, Object> vars = new HashMap<>();
        vars.put("productId", str(event.getProductId()));
        vars.put("variantId", str(event.getVariantId()));
        vars.put("productName", str(event.getProductName()));
        vars.put("currentStock", String.valueOf(event.getCurrentStock()));
        vars.put("threshold", String.valueOf(event.getThreshold()));

        // Send to admin — recipient can be configured via template or env variable
        sendNotification("admin@backandwhite.com", "Stock Low Alert", "stock-low-alert", vars);
    }

    // ─── Gift Card Events ────────────────────────────────────────────

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_GIFT_CARD_PURCHASED, groupId = AppConstants.KAFKA_GROUP_NOTIFICATIONS, containerFactory = "avroKafkaListenerContainerFactory")
    public void onGiftCardPurchased(GiftCardPurchasedEvent event) {
        String recipientEmail = str(event.getRecipientEmail());
        if (recipientEmail == null) {
            log.warn("::> gift-card.purchased without recipientEmail, skipping. giftCardId={}",
                    str(event.getGiftCardId()));
            return;
        }
        log.info("::> Notification: giftcard.purchased giftCardId={}, recipient={}", str(event.getGiftCardId()),
                recipientEmail);

        String rawExpiry = str(event.getExpiryDate());
        String formattedExpiry = rawExpiry;
        if (rawExpiry != null && !rawExpiry.isBlank()) {
            try {
                formattedExpiry = LocalDate.parse(rawExpiry)
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ex) {
                log.warn("::> Could not parse expiryDate '{}', using raw value", rawExpiry);
            }
        }

        String cardGradient = designGradient(str(event.getDesignId()));

        Map<String, Object> vars = new HashMap<>();
        vars.put("recipientName", str(event.getRecipientName()));
        vars.put("buyerName", str(event.getBuyerName()));
        vars.put("code", str(event.getCode()));
        vars.put("amount", str(event.getAmount()));
        vars.put("currency", str(event.getCurrency()) != null ? str(event.getCurrency()) : "USD");
        vars.put("message", str(event.getMessage()));
        vars.put("expiryDate", formattedExpiry);
        vars.put("cardGradient", cardGradient);
        vars.put("storeUrl", storeUrl + giftcardActivatePath);

        sendNotification(recipientEmail, "¡Has recibido una Tarjeta Regalo!", "gift-card-purchased", vars);
    }

    // ─── Common ─────────────────────────────────────────────────────

    private void sendNotification(String recipient, String subject, String templateName,
            Map<String, Object> variables) {
        try {
            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .subject(subject)
                    .type(NotificationType.EMAIL)
                    .status(NotificationStatus.PENDING)
                    .variables(variables)
                    .retryCount(0)
                    .build();

            notificationCommandHandler.validate(notification);

            Optional<NotificationTemplate> templateOpt = notificationTemplateUseCase.findByName(templateName);
            if (templateOpt.isEmpty()) {
                log.warn("::> Template '{}' not found. Using default template.", templateName);
            } else {
                NotificationTemplate template = templateOpt.get();
                if (Boolean.FALSE.equals(template.getActive())) {
                    log.warn("::> Template '{}' is inactive. Using default.", templateName);
                } else {
                    notification.setTemplate(template);
                    if (notification.getSubject() == null || notification.getSubject().isBlank()) {
                        notification.setSubject(template.getSubject());
                    }
                }
            }

            Notification saved = notificationRepository.save(notification);
            emailService.sendEmail(saved);
            log.info("::> Notification sent: id={}, template={}, recipient={}", saved.getId(), templateName, recipient);
        } catch (Exception e) {
            log.error("::> Failed to send notification: template={}, recipient={}, error={}", templateName, recipient,
                    e.getMessage(), e);
        }
    }

    private String str(CharSequence cs) {
        return cs != null ? cs.toString() : null;
    }

    private String designGradient(String designId) {
        if (designId == null)
            return "linear-gradient(135deg, #111827 0%, #374151 100%)";
        return switch (designId) {
            case "premium" -> "linear-gradient(135deg, #78350F 0%, #D97706 100%)";
            case "birthday" -> "linear-gradient(135deg, #4C1D95 0%, #7C3AED 100%)";
            case "love" -> "linear-gradient(135deg, #9D174D 0%, #EC4899 100%)";
            case "nature" -> "linear-gradient(135deg, #064E3B 0%, #059669 100%)";
            case "ocean" -> "linear-gradient(135deg, #1E3A5F 0%, #0EA5E9 100%)";
            default -> "linear-gradient(135deg, #111827 0%, #374151 100%)";
        };
    }
}
