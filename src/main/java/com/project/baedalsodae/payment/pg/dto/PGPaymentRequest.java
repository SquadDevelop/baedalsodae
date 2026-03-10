package com.project.baedalsodae.payment.pg.dto;

import com.project.baedalsodae.payment.entity.PaymentMethod;
import java.util.UUID;

public record PGPaymentRequest(
        UUID orderId, UUID userId, PaymentMethod paymentMethod, long amount) {}
