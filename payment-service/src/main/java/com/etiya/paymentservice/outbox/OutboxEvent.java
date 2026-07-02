package com.etiya.paymentservice.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A row in the Transactional Outbox table (PostgreSQL, {@code paymentdb}), captured by Debezium CDC.
 *
 * <p>The business layer inserts one row here inside the same transaction as the payment write. It is
 * <strong>insert-only</strong>: publishing to Kafka is done by Debezium's
 * {@code io.debezium.transforms.outbox.EventRouter}, which reads the WAL and forwards each insert to
 * the {@code payment-created} topic.</p>
 *
 * <p>Column names match Debezium's Outbox Event Router default convention
 * ({@code id}, {@code aggregatetype}, {@code aggregateid}, {@code type}, {@code payload}); see
 * order-service's {@code OutboxEvent} for the full rationale.</p>
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    /** Event id; also emitted by Debezium as a header for consumer-side idempotency. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Domain aggregate the event belongs to, e.g. {@code "Payment"}. Used for topic routing. */
    @Column(name = "aggregatetype", nullable = false)
    private String aggregateType;

    /** Identifier of the aggregate instance, e.g. the payment id. Becomes the Kafka message key. */
    @Column(name = "aggregateid", nullable = false)
    private String aggregateId;

    /** Logical event name, e.g. {@code "PaymentCreated"}. */
    @Column(name = "type", nullable = false)
    private String type;

    /** Serialized (JSON) event body, emitted to Kafka as-is. */
    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    /** When the event was recorded; diagnostics only, not required by Debezium. */
    @Column(name = "createdat", nullable = false)
    private Instant createdAt;

    /** Required by JPA. */
    protected OutboxEvent() {
    }

    public OutboxEvent(String aggregateType, String aggregateId, String type,
                       String payload, Instant createdAt) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.type = type;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
