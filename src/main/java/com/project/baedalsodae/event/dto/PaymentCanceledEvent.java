package com.project.baedalsodae.event.dto;

import com.project.baedalsodae.payment.entity.Payment;
import com.project.baedalsodae.payment.entity.PaymentStatus;

import java.util.UUID;

public record PaymentCanceledEvent(
        UUID orderId, UUID paymentId, PaymentStatus status, String pgTransactionId, UUID userId) {
    public static PaymentCanceledEvent from(final Payment payment) {
        return new PaymentCanceledEvent(
                payment.getOrderId(),
                payment.getId(),
                payment.getStatus(),
                payment.getPgTransactionId(),
                payment.getUserId());
    }
}
