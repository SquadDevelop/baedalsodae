package com.project.baedalsodae.event.dto;

import com.project.baedalsodae.payment.entity.Payment;
import com.project.baedalsodae.payment.entity.PaymentStatus;
import java.util.UUID;

public record PaymentCreatedEvent(
        UUID orderId, UUID paymentId, PaymentStatus status, String pgTransactionId) {
    public static PaymentCreatedEvent from(final Payment payment) {
        return new PaymentCreatedEvent(
                payment.getOrderId(),
                payment.getId(),
                payment.getStatus(),
                payment.getPgTransactionId());
    }
}
