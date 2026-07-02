package com.etiya.paymentservice.events;

import java.math.BigDecimal;

/**
 * Event consumed from the {@code order-created} Kafka topic. Mirrors the payload published by
 * order-service so payment-service can create a payment without calling back into order-service.
 */
public record OrderCreatedEvent(
        int orderId,
        int customerId,
        int productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        String address) {
}
