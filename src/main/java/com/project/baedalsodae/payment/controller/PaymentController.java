package com.project.baedalsodae.payment.controller;

import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.global.common.TimeCursorPage;
import com.project.baedalsodae.payment.dto.response.PaymentResponse;
import com.project.baedalsodae.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<TimeCursorPage<List<PaymentResponse>>>> getPayments(
            Instant cursor, int size) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.PAYMENT_HISTORY_FOUND,
                        paymentService.getPayments(cursor, size)));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.PAYMENT_DETAIL_FOUND, paymentService.getPayment(paymentId)));
    }
}
