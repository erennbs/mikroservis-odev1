package com.etiya.paymentservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Payment domain entity, persisted to PostgreSQL ({@code payments} table in {@code paymentdb}).
 *
 * <p>A payment is created either directly through the CRUD API or automatically when an
 * OrderCreated event is consumed. In both cases the payment write and the
 * {@link com.etiya.paymentservice.outbox.OutboxEvent outbox} write happen in the same DB
 * transaction, so a PaymentCreated event is durably queued atomically with the payment. Debezium
 * (CDC) then streams the outbox insert from Postgres' WAL to the {@code payment-created} topic.</p>
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int orderId;

    @Column(nullable = false)
    private int customerId;

    @Column(nullable = false)
    private BigDecimal amount;

    /** Payment status, e.g. {@code COMPLETED} / {@code PENDING} / {@code FAILED}. */
    @Column(nullable = false)
    private String status;

    public Payment() {
    }

    public Payment(int id, int orderId, int customerId, BigDecimal amount, String status) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
