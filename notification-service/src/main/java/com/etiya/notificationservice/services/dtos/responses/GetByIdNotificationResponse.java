package com.etiya.notificationservice.services.dtos.responses;

import java.time.Instant;

public class GetByIdNotificationResponse {

    private int id;
    private int customerId;
    private int orderId;
    private String message;
    private Instant createdAt;

    public GetByIdNotificationResponse() {
    }

    public GetByIdNotificationResponse(int id, int customerId, int orderId, String message, Instant createdAt) {
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
