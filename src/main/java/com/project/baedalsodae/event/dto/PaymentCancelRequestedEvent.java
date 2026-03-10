package com.project.baedalsodae.event.dto;

import com.project.baedalsodae.payment.entity.Payment;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCancelRequestedEvent(UUID orderId, UUID userId, BigDecimal finalAmount) {
    public static PaymentCancelRequestedEvent from(Payment payment) {
        return new PaymentCancelRequestedEvent(
                payment.getId(), payment.getUserId(), payment.getAmount());
    }
}
