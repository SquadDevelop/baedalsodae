package com.project.baedalsodae.payment.pg.dto;

import com.project.baedalsodae.payment.entity.PaymentMethod;
import java.math.BigDecimal;
import java.util.UUID;

public record PGCancelRequest(
        UUID orderId,
        UUID userId,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        String pgTransactionId) {}
