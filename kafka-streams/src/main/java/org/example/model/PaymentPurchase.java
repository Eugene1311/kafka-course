package org.example.model;

public record PaymentPurchase(
        Integer purchaseId,
        String purchaseProductName,
        String paymentStatus
) {
}
