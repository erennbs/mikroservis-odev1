package com.etiya.notificationservice.services.dtos.responses;

public class DeletedNotificationResponse {

    private int id;
    private int customerId;

    public DeletedNotificationResponse() {
    }

    public DeletedNotificationResponse(int id, int customerId) {
        this.id = id;
        this.customerId = customerId;
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
}
