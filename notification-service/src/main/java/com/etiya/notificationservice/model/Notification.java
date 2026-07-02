package com.etiya.notificationservice.model;

import java.time.Instant;

/**
 * In-memory notification record. Deliberately NOT a JPA entity: notification-service has no database,
 * so notifications live only in {@link com.etiya.notificationservice.store.NotificationStore} for the
 * lifetime of the process.
 */
public class Notification {

    private int id;
    private int customerId;
    private int orderId;
    private String message;
    private Instant createdAt;

    public Notification() {
    }

    public Notification(int id, int customerId, int orderId, String message, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.orderId = orderId;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
