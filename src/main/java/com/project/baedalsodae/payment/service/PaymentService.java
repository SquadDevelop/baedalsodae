package com.project.baedalsodae.payment.service;

import com.project.baedalsodae.global.common.TimeCursorPage;
import com.project.baedalsodae.payment.dto.response.PaymentResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public interface PaymentService {

    TimeCursorPage<PaymentResponse> getPayments(Instant cursor, int size);

    PaymentResponse getPayment(UUID paymentId);
}
