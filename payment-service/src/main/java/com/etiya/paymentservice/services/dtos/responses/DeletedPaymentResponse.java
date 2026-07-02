package com.etiya.paymentservice.services.dtos.responses;

public class DeletedPaymentResponse {

    private int id;
    private int orderId;

    public DeletedPaymentResponse() {
    }

    public DeletedPaymentResponse(int id, int orderId) {
        this.id = id;
        this.orderId = orderId;
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
}
