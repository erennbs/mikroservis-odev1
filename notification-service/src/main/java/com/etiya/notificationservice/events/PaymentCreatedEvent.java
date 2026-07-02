package com.etiya.notificationservice.events;

import java.math.BigDecimal;

/**
 * Event consumed from the {@code payment-created} Kafka topic. Mirrors the payload published by
 * payment-service so notification-service can raise a notification without calling back.
 */
public record PaymentCreatedEvent(
        int paymentId,
        int orderId,
        int customerId,
        BigDecimal amount,
        String status) {
}
