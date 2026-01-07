package com.pagamentos.model;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    REFUNDED,
    CANCELLED,
    REQUIRES_ACTION
}