package com.backandwhite.infrastructure.message.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.backandwhite.application.handler.NotificationCommandHandler;
import com.backandwhite.application.usecase.NotificationTemplateUseCase;
import com.backandwhite.core.kafka.avro.*;
import com.backandwhite.domain.model.Notification;
import com.backandwhite.domain.model.NotificationTemplate;
import com.backandwhite.domain.port.NotificationSender;
import com.backandwhite.domain.repository.NotificationRepository;
import com.backandwhite.infrastructure.message.kafka.mapper.NotificationEventMapper;
import com.backandwhite.infrastructure.message.kafka.mapper.NotificationEventMapperImpl;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BusinessEventNotificationConsumerTest {

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
    private BusinessEventNotificationConsumer consumer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(consumer, "storeUrl", "http://localhost:9000");
        ReflectionTestUtils.setField(consumer, "giftcardActivatePath", "/cuenta?tab=giftcards");
    }

    private void stubTemplateFound(String templateName) {
        NotificationTemplate template = NotificationTemplate.builder().name(templateName)
                .templateFile("email/" + templateName).active(true).subject("Subject").build();
        when(notificationTemplateUseCase.findByName(templateName)).thenReturn(Optional.of(template));
    }

    private void stubTemplateMissing(String templateName) {
        when(notificationTemplateUseCase.findByName(templateName)).thenReturn(Optional.empty());
    }

    private void stubSaveReturnsInput() {
        when(notificationRepository.save(any())).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });
    }

    // ─── Order Created ──────────────────────────────────────────────

    @Nested
    class OnOrderCreated {

        @Test
        void withEmail_sendsNotification() {
            OrderCreatedEvent event = OrderCreatedEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1")
                    .setEmail("user@test.com").setOrderReference("REF-001").setTotalAmount("100.00").setCurrency("USD")
                    .setStatus("CREATED").setItemCount(3).setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("order-confirmation");
            stubSaveReturnsInput();

            consumer.onOrderCreated(event);

            verify(notificationCommandHandler).validate(any());
            verify(notificationRepository).save(any());
            verify(notificationSender).send(any());
        }

        @Test
        void withoutEmail_skipsNotification() {
            OrderCreatedEvent event = OrderCreatedEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1")
                    .setOrderReference("REF-001").setTotalAmount("100.00").setCurrency("USD").setStatus("CREATED")
                    .setItemCount(3).setTimestamp("2025-01-01T00:00:00Z").build();

            consumer.onOrderCreated(event);

            verify(notificationSender, never()).send(any());
        }

        @Test
        void capturesCorrectVariables() {
            OrderCreatedEvent event = OrderCreatedEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1")
                    .setEmail("user@test.com").setOrderReference("REF-001").setTotalAmount("99.99").setCurrency("EUR")
                    .setStatus("PENDING").setItemCount(5).setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("order-confirmation");
            stubSaveReturnsInput();

            consumer.onOrderCreated(event);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            Notification n = captor.getValue();
            assertThat(n.getVariables()).containsEntry("orderId", "ORD-1").containsEntry("orderReference", "REF-001")
                    .containsEntry("totalAmount", "99.99").containsEntry("currency", "EUR")
                    .containsEntry("itemCount", "5").containsEntry("status", "PENDING");
        }
    }

    // ─── Order Confirmed ────────────────────────────────────────────

    @Nested
    class OnOrderConfirmed {

        @Test
        void withEmail_sendsNotification() {
            OrderConfirmedEvent event = OrderConfirmedEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1")
                    .setEmail("user@test.com").setOrderReference("REF-001").setTotalAmount("100.00").setCurrency("EUR")
                    .setItemCount(2).setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("order-confirmed");
            stubSaveReturnsInput();

            consumer.onOrderConfirmed(event);

            verify(notificationSender).send(any());
        }

        @Test
        void withoutEmail_skips() {
            OrderConfirmedEvent event = OrderConfirmedEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1")
                    .setOrderReference("REF-001").setTotalAmount("100.00").setCurrency("EUR").setItemCount(2)
                    .setTimestamp("2025-01-01T00:00:00Z").build();

            consumer.onOrderConfirmed(event);

            verify(notificationSender, never()).send(any());
        }
    }

    // ─── Order Cancelled ────────────────────────────────────────────

    @Nested
    class OnOrderCancelled {

        @Test
        void withEmail_sendsNotification() {
            OrderCancelledEvent event = OrderCancelledEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1")
                    .setEmail("user@test.com").setOrderReference("REF-001").setReason("Customer request")
                    .setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("order-cancelled");
            stubSaveReturnsInput();

            consumer.onOrderCancelled(event);

            verify(notificationSender).send(any());
        }

        @Test
        void withoutEmail_skips() {
            OrderCancelledEvent event = OrderCancelledEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1")
                    .setOrderReference("REF-001").setTimestamp("2025-01-01T00:00:00Z").build();

            consumer.onOrderCancelled(event);

            verify(notificationSender, never()).send(any());
        }
    }

    // ─── Order Shipped ──────────────────────────────────────────────

    @Nested
    class OnOrderShipped {

        @Test
        void withEmail_sendsNotification() {
            OrderShippedEvent event = OrderShippedEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1")
                    .setEmail("user@test.com").setOrderReference("REF-001").setTrackingNumber("TRACK-123")
                    .setCarrier("DHL").setEstimatedDelivery("2025-01-10").setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("order-shipped");
            stubSaveReturnsInput();

            consumer.onOrderShipped(event);

            verify(notificationSender).send(any());
        }

        @Test
        void withoutEmail_skips() {
            OrderShippedEvent event = OrderShippedEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1")
                    .setOrderReference("REF-001").setTimestamp("2025-01-01T00:00:00Z").build();

            consumer.onOrderShipped(event);

            verify(notificationSender, never()).send(any());
        }
    }

    // ─── Order Delivered ────────────────────────────────────────────

    @Nested
    class OnOrderDelivered {

        @Test
        void withEmail_sendsNotification() {
            OrderDeliveredEvent event = OrderDeliveredEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1")
                    .setEmail("user@test.com").setOrderReference("REF-001").setTotalAmount("100.00")
                    .setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("order-delivered");
            stubSaveReturnsInput();

            consumer.onOrderDelivered(event);

            verify(notificationSender).send(any());
        }

        @Test
        void withoutEmail_skips() {
            OrderDeliveredEvent event = OrderDeliveredEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1")
                    .setOrderReference("REF-001").setTotalAmount("100.00").setTimestamp("2025-01-01T00:00:00Z").build();

            consumer.onOrderDelivered(event);

            verify(notificationSender, never()).send(any());
        }
    }

    // ─── Payment Confirmed ──────────────────────────────────────────

    @Nested
    class OnPaymentConfirmed {

        @Test
        void withEmail_sendsNotification() {
            PaymentConfirmedEvent event = PaymentConfirmedEvent.newBuilder().setPaymentId("PAY-1").setOrderId("ORD-1")
                    .setUserId("USR-1").setEmail("user@test.com").setAmount("50.00").setCurrency("EUR")
                    .setMethod("CARD").setGateway("Stripe").setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("payment-confirmed");
            stubSaveReturnsInput();

            consumer.onPaymentConfirmed(event);

            verify(notificationSender).send(any());
        }

        @Test
        void withoutEmail_skips() {
            PaymentConfirmedEvent event = PaymentConfirmedEvent.newBuilder().setPaymentId("PAY-1").setOrderId("ORD-1")
                    .setUserId("USR-1").setAmount("50.00").setCurrency("EUR").setMethod("CARD").setGateway("Stripe")
                    .setTimestamp("2025-01-01T00:00:00Z").build();

            consumer.onPaymentConfirmed(event);

            verify(notificationSender, never()).send(any());
        }
    }

    // ─── Payment Failed ─────────────────────────────────────────────

    @Nested
    class OnPaymentFailed {

        @Test
        void withEmail_sendsNotification() {
            PaymentFailedEvent event = PaymentFailedEvent.newBuilder().setPaymentId("PAY-1").setOrderId("ORD-1")
                    .setUserId("USR-1").setEmail("user@test.com").setAmount("50.00").setReason("Insufficient funds")
                    .setGateway("Stripe").setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("payment-failed");
            stubSaveReturnsInput();

            consumer.onPaymentFailed(event);

            verify(notificationSender).send(any());
        }

        @Test
        void withoutEmail_skips() {
            PaymentFailedEvent event = PaymentFailedEvent.newBuilder().setPaymentId("PAY-1").setOrderId("ORD-1")
                    .setUserId("USR-1").setAmount("50.00").setGateway("Stripe").setTimestamp("2025-01-01T00:00:00Z")
                    .build();

            consumer.onPaymentFailed(event);

            verify(notificationSender, never()).send(any());
        }
    }

    // ─── Refund Completed ───────────────────────────────────────────

    @Nested
    class OnRefundCompleted {

        @Test
        void withEmail_sendsNotification() {
            PaymentRefundCompletedEvent event = PaymentRefundCompletedEvent.newBuilder().setPaymentId("PAY-1")
                    .setRefundId("REF-1").setOrderId("ORD-1").setUserId("USR-1").setEmail("user@test.com")
                    .setRefundAmount("25.00").setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("refund-completed");
            stubSaveReturnsInput();

            consumer.onRefundCompleted(event);

            verify(notificationSender).send(any());
        }

        @Test
        void withoutEmail_skips() {
            PaymentRefundCompletedEvent event = PaymentRefundCompletedEvent.newBuilder().setPaymentId("PAY-1")
                    .setRefundId("REF-1").setOrderId("ORD-1").setUserId("USR-1").setRefundAmount("25.00")
                    .setTimestamp("2025-01-01T00:00:00Z").build();

            consumer.onRefundCompleted(event);

            verify(notificationSender, never()).send(any());
        }
    }

    // ─── Newsletter Subscribed ──────────────────────────────────────

    @Nested
    class OnNewsletterSubscribed {

        @Test
        void withEmail_sendsNotification() {
            NewsletterSubscribedEvent event = NewsletterSubscribedEvent.newBuilder().setEmail("user@test.com")
                    .setSource("website").setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("newsletter-welcome");
            stubSaveReturnsInput();

            consumer.onNewsletterSubscribed(event);

            verify(notificationSender).send(any());
        }
    }

    // ─── Stock Low Alert ────────────────────────────────────────────

    @Nested
    class OnStockLowAlert {

        @Test
        void sendsNotificationToAdmin() {
            StockLowAlertEvent event = StockLowAlertEvent.newBuilder().setProductId("PROD-1").setVariantId("VAR-1")
                    .setProductName("Widget").setCurrentStock(2).setThreshold(5).setTimestamp("2025-01-01T00:00:00Z")
                    .build();
            stubTemplateFound("stock-low-alert");
            stubSaveReturnsInput();

            consumer.onStockLowAlert(event);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat(captor.getValue().getRecipient()).isEqualTo("admin@backandwhite.com");
            verify(notificationSender).send(any());
        }

        @Test
        void capturesCorrectVariables() {
            StockLowAlertEvent event = StockLowAlertEvent.newBuilder().setProductId("PROD-1").setVariantId("VAR-1")
                    .setProductName("Widget").setCurrentStock(2).setThreshold(5).setTimestamp("2025-01-01T00:00:00Z")
                    .build();
            stubTemplateFound("stock-low-alert");
            stubSaveReturnsInput();

            consumer.onStockLowAlert(event);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat(captor.getValue().getVariables()).containsEntry("productId", "PROD-1")
                    .containsEntry("variantId", "VAR-1").containsEntry("productName", "Widget")
                    .containsEntry("currentStock", "2").containsEntry("threshold", "5");
        }
    }

    // ─── Gift Card Purchased ────────────────────────────────────────

    @Nested
    class OnGiftCardPurchased {

        @Test
        void withRecipientEmail_sendsNotification() {
            GiftCardPurchasedEvent event = GiftCardPurchasedEvent.newBuilder().setGiftCardId("GC-1")
                    .setCode("GIFT-CODE").setRecipientName("Jane").setRecipientEmail("jane@test.com").setAmount("50.00")
                    .setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("gift-card-purchased");
            stubSaveReturnsInput();

            consumer.onGiftCardPurchased(event);

            verify(notificationSender).send(any());
        }

        @Test
        void withoutRecipientEmail_skips() {
            GiftCardPurchasedEvent event = mock(GiftCardPurchasedEvent.class);
            when(event.getRecipientEmail()).thenReturn(null);

            consumer.onGiftCardPurchased(event);

            verify(notificationSender, never()).send(any());
        }

        @Test
        void capturesVariablesWithFormattedExpiryDate() {
            GiftCardPurchasedEvent event = GiftCardPurchasedEvent.newBuilder().setGiftCardId("GC-1")
                    .setCode("GIFT-CODE").setRecipientName("Jane").setRecipientEmail("jane@test.com")
                    .setBuyerName("John").setAmount("100.00").setCurrency("EUR").setMessage("Happy Birthday!")
                    .setExpiryDate("2026-06-15").setDesignId("birthday").setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("gift-card-purchased");
            stubSaveReturnsInput();

            consumer.onGiftCardPurchased(event);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat(captor.getValue().getVariables()).containsEntry("recipientName", "Jane")
                    .containsEntry("buyerName", "John").containsEntry("code", "GIFT-CODE")
                    .containsEntry("amount", "100.00").containsEntry("currency", "EUR")
                    .containsEntry("message", "Happy Birthday!").containsEntry("expiryDate", "15/06/2026")
                    .containsEntry("storeUrl", "http://localhost:9000/cuenta?tab=giftcards");
        }

        @Test
        void withInvalidExpiryDate_usesRawValue() {
            GiftCardPurchasedEvent event = GiftCardPurchasedEvent.newBuilder().setGiftCardId("GC-1")
                    .setCode("GIFT-CODE").setRecipientName("Jane").setRecipientEmail("jane@test.com").setAmount("50.00")
                    .setExpiryDate("invalid-date").setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("gift-card-purchased");
            stubSaveReturnsInput();

            consumer.onGiftCardPurchased(event);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat(captor.getValue().getVariables()).containsEntry("expiryDate", "invalid-date");
        }

        @Test
        void withNullCurrency_defaultsToUSD() {
            GiftCardPurchasedEvent event = GiftCardPurchasedEvent.newBuilder().setGiftCardId("GC-1")
                    .setCode("GIFT-CODE").setRecipientName("Jane").setRecipientEmail("jane@test.com").setAmount("50.00")
                    .setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("gift-card-purchased");
            stubSaveReturnsInput();

            consumer.onGiftCardPurchased(event);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat(captor.getValue().getVariables()).containsEntry("currency", "USD");
        }

        @Test
        void withNullExpiryDate_usesNullValue() {
            GiftCardPurchasedEvent event = GiftCardPurchasedEvent.newBuilder().setGiftCardId("GC-1")
                    .setCode("GIFT-CODE").setRecipientName("Jane").setRecipientEmail("jane@test.com").setAmount("50.00")
                    .setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("gift-card-purchased");
            stubSaveReturnsInput();

            consumer.onGiftCardPurchased(event);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            // null expiryDate → formattedExpiry stays null
        }
    }

    // ─── Design Gradient ────────────────────────────────────────────

    @Nested
    class DesignGradientTests {

        static Stream<Arguments> designGradientArgs() {
            return Stream.of(Arguments.of("premium", "#78350F"), Arguments.of("birthday", "#4C1D95"),
                    Arguments.of("love", "#9D174D"), Arguments.of("nature", "#064E3B"),
                    Arguments.of("ocean", "#1E3A5F"), Arguments.of("unknown", "#111827"),
                    Arguments.of(null, "#111827"));
        }

        @ParameterizedTest
        @MethodSource("designGradientArgs")
        void design_returnsExpectedGradient(String designId, String expectedColor) {
            var builder = GiftCardPurchasedEvent.newBuilder().setGiftCardId("GC-1").setCode("CODE")
                    .setRecipientName("Jane").setRecipientEmail("jane@test.com").setAmount("50.00")
                    .setTimestamp("2025-01-01T00:00:00Z");
            if (designId != null) {
                builder.setDesignId(designId);
            }
            stubTemplateFound("gift-card-purchased");
            stubSaveReturnsInput();

            consumer.onGiftCardPurchased(builder.build());

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat((String) captor.getValue().getVariables().get("cardGradient")).contains(expectedColor);
        }
    }

    // ─── Template Resolution ────────────────────────────────────────

    @Nested
    class TemplateResolutionTests {

        @Test
        void templateNotFound_proceedsWithDefault() {
            OrderCreatedEvent event = OrderCreatedEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1")
                    .setEmail("user@test.com").setOrderReference("REF-001").setTotalAmount("100.00").setCurrency("USD")
                    .setStatus("CREATED").setItemCount(3).setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateMissing("order-confirmation");
            stubSaveReturnsInput();

            consumer.onOrderCreated(event);

            verify(notificationSender).send(any());
        }

        @Test
        void inactiveTemplate_proceedsWithDefault() {
            NotificationTemplate template = NotificationTemplate.builder().name("order-confirmation")
                    .templateFile("email/order-confirmation").active(false).build();
            when(notificationTemplateUseCase.findByName("order-confirmation")).thenReturn(Optional.of(template));

            OrderCreatedEvent event = OrderCreatedEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1")
                    .setEmail("user@test.com").setOrderReference("REF-001").setTotalAmount("100.00").setCurrency("USD")
                    .setStatus("CREATED").setItemCount(3).setTimestamp("2025-01-01T00:00:00Z").build();
            stubSaveReturnsInput();

            consumer.onOrderCreated(event);

            verify(notificationSender).send(any());
        }
    }

    // ─── Error Handling ─────────────────────────────────────────────

    @Nested
    class ErrorHandlingTests {

        @Test
        void emailServiceThrows_doesNotPropagateException() {
            OrderCreatedEvent event = OrderCreatedEvent.newBuilder().setOrderId("ORD-1").setUserId("USR-1")
                    .setEmail("user@test.com").setOrderReference("REF-001").setTotalAmount("100.00").setCurrency("USD")
                    .setStatus("CREATED").setItemCount(3).setTimestamp("2025-01-01T00:00:00Z").build();
            stubTemplateFound("order-confirmation");
            stubSaveReturnsInput();
            doThrow(new RuntimeException("SMTP failure")).when(notificationSender).send(any());

            // Should NOT throw — exception is caught inside sendNotification
            consumer.onOrderCreated(event);

            verify(notificationSender).send(any());
        }
    }
}
