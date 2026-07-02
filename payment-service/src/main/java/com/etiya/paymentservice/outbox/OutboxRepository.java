package com.etiya.paymentservice.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA access to the outbox table.
 *
 * <p>Only inserts are performed here; Debezium reads the resulting WAL entries and publishes them
 * to the {@code payment-created} Kafka topic, so no polling/query-by-status method is needed.</p>
 */
public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
}
