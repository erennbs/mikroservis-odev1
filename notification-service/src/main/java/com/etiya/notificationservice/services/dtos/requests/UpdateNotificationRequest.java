package com.etiya.notificationservice.services.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class UpdateNotificationRequest {

    @Positive(message = "Id must be greater than zero")
    private int id;

    @Positive(message = "Customer id must be greater than zero")
    private int customerId;

    @Positive(message = "Order id must be greater than zero")
    private int orderId;

    @NotBlank(message = "Message must not be blank")
    private String message;

    public UpdateNotificationRequest() {
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
}
