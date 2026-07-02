package com.etiya.notificationservice.messaging;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import com.etiya.notificationservice.events.PaymentCreatedEvent;
import com.etiya.notificationservice.model.Notification;
import com.etiya.notificationservice.store.NotificationStore;

/**
 * Spring Cloud Stream consumer. The bean name {@code paymentCreated} is referenced by
 * {@code spring.cloud.function.definition} and bound to the input binding
 * {@code paymentCreated-in-0} (Kafka topic "payment-created") in the config-server config.
 *
 * <p>On each PaymentCreated event a notification is stored in-memory (via {@link NotificationStore}).</p>
 *
 * <p><strong>Idempotency (best-effort):</strong> the other consumers use a persistent Inbox table,
 * but notification-service has no database, so de-duplication here is an in-memory {@link Set} of
 * already-seen {@code eventId}s. It suppresses duplicate deliveries only for the lifetime of the
 * process; after a restart a redelivery could produce a second notification. That trade-off is
 * acceptable because notifications are non-critical and non-durable by design.</p>
 */
@Configuration
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    /** See order-service's outbox connector: Debezium places the event UUID in this header. */
    private static final String DEBEZIUM_ID_HEADER = "eventId";
    private static final String EVENT_TYPE_FALLBACK_PREFIX = "PaymentCreated:";

    /** Best-effort, in-memory de-duplication of processed event ids (no DB available). */
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    @Bean
    public Consumer<Message<PaymentCreatedEvent>> paymentCreated(NotificationStore notificationStore) {
        return message -> {
            PaymentCreatedEvent event = message.getPayload();
            String messageId = resolveMessageId(message, event);

            if (!processedEventIds.add(messageId)) {
                log.info("Duplicate PaymentCreated skipped (messageId={}, paymentId={})",
                        messageId, event.paymentId());
                return;
            }

            String text = String.format(
                    "Payment #%d for order #%d completed. Amount=%s, status=%s",
                    event.paymentId(), event.orderId(), event.amount(), event.status());
            Notification notification =
                    notificationStore.create(event.customerId(), event.orderId(), text);

            log.info("Notification created (id={}, customerId={}, orderId={}): {}",
                    notification.getId(), notification.getCustomerId(), notification.getOrderId(), text);
        };
    }

    /**
     * Resolves the de-duplication key from the Debezium {@code eventId} header, falling back to a
     * deterministic business key ({@code PaymentCreated:<paymentId>}) if the header is absent.
     */
    private String resolveMessageId(Message<?> message, PaymentCreatedEvent event) {
        Object header = message.getHeaders().get(DEBEZIUM_ID_HEADER);
        String messageId = null;
        if (header instanceof byte[] bytes) {
            messageId = new String(bytes, StandardCharsets.UTF_8);
        } else if (header != null) {
            messageId = header.toString();
        }
        if (messageId == null || messageId.isBlank()) {
            messageId = EVENT_TYPE_FALLBACK_PREFIX + event.paymentId();
            log.warn("Debezium '{}' header missing; falling back to business key '{}'",
                    DEBEZIUM_ID_HEADER, messageId);
        }
        return messageId;
    }
}
