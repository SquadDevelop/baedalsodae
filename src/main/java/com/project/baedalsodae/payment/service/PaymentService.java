package com.project.baedalsodae.payment.service;

import com.project.baedalsodae.global.common.TimeCursorPage;
import com.project.baedalsodae.payment.dto.response.PaymentResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PaymentService {

    TimeCursorPage<List<PaymentResponse>> getPayments(Instant cursor, int size);

    PaymentResponse getPayment(UUID paymentId);

    void processPayment(UUID orderId, UUID userId, BigDecimal finalAmount);

    void processPaymentCancel(UUID orderId, UUID userId, BigDecimal finalAmount);
}
