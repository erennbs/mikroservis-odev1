package com.etiya.paymentservice.inbox;

import com.etiya.paymentservice.events.OrderCreatedEvent;
import com.etiya.paymentservice.services.abstracts.PaymentService;
import com.etiya.paymentservice.services.dtos.requests.CreatePaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Applies the OrderCreated side effect exactly once, using the Inbox pattern.
 *
 * <p>Delivery is at-least-once, so the same event may arrive more than once. Everything below runs
 * in ONE transaction:</p>
 * <ol>
 *   <li>If {@code messageId} is already in {@link ProcessedMessageRepository}, skip — it was handled.</li>
 *   <li>Otherwise create a Payment (via {@link PaymentService#add}, which also records the
 *       PaymentCreated outbox event) and insert the inbox row.</li>
 * </ol>
 *
 * <p>The pre-check eliminates most duplicates cheaply. The real guarantee is the {@code message_id}
 * primary key: if two deliveries race past the check, the second {@code save} violates the
 * constraint, the whole transaction rolls back (so no second payment is created), the Kafka offset
 * is not committed, and on the ensuing redelivery the pre-check finds the row and skips. Net effect:
 * exactly one payment per order.</p>
 */
@Service
public class OrderCreatedInboxHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedInboxHandler.class);
    private static final String CONSUMER = "payment-service";
    private static final String EVENT_TYPE = "OrderCreated";
    /** Payments auto-created from an order are considered settled immediately in this demo. */
    private static final String DEFAULT_STATUS = "COMPLETED";

    private final ProcessedMessageRepository processedRepository;
    private final PaymentService paymentService;

    public OrderCreatedInboxHandler(ProcessedMessageRepository processedRepository,
                                    PaymentService paymentService) {
        this.processedRepository = processedRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public void handle(String messageId, OrderCreatedEvent event) {
        if (processedRepository.existsById(messageId)) {
            log.info("Duplicate OrderCreated skipped (messageId={}, orderId={})",
                    messageId, event.orderId());
            return;
        }

        createPayment(event);

        processedRepository.save(
                new ProcessedMessage(messageId, CONSUMER, EVENT_TYPE, Instant.now()));

        log.info("OrderCreated processed (messageId={}, orderId={}, customerId={}, amount={})",
                messageId, event.orderId(), event.customerId(), event.totalPrice());
    }

    /** Business side effect: create a payment for the order's total amount. */
    private void createPayment(OrderCreatedEvent event) {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(event.orderId());
        request.setCustomerId(event.customerId());
        request.setAmount(event.totalPrice());
        request.setStatus(DEFAULT_STATUS);

        paymentService.add(request);
    }
}
