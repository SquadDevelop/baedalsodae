package com.project.baedalsodae.payment.dto.event;

import com.project.baedalsodae.payment.entity.Payment;
import com.project.baedalsodae.payment.entity.PaymentStatus;
import java.util.UUID;

public record PaymentResultEvent(
        UUID orderId, UUID paymentId, PaymentStatus status, String pgTransactionId) {
    public static PaymentResultEvent from(final Payment payment) {
        return new PaymentResultEvent(
                payment.getOrder(),
                payment.getId(),
                payment.getStatus(),
                payment.getPgTransactionId());
    }
}
