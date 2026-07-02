package com.etiya.paymentservice.events;

import java.math.BigDecimal;

/**
 * Event published to Kafka whenever a payment is created (via the CRUD API or automatically from an
 * OrderCreated event). Carries the full payment detail so downstream services (e.g.
 * notification-service) can react without calling back into payment-service.
 */
public record PaymentCreatedEvent(
        int paymentId,
        int orderId,
        int customerId,
        BigDecimal amount,
        String status) {
}
