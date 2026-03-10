package com.project.baedalsodae.payment.pg.service.impl;

import com.project.baedalsodae.payment.pg.dto.PGCancelRequest;
import com.project.baedalsodae.payment.pg.dto.PGPaymentRequest;
import com.project.baedalsodae.payment.pg.dto.PGPaymentResponse;
import com.project.baedalsodae.payment.pg.enums.PGProviderType;
import com.project.baedalsodae.payment.pg.service.PGClient;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockPGClient implements PGClient {

    private static final BigDecimal PAYMENT_LIMIT = new BigDecimal("1000000"); // 1 million

    @Override
    public PGProviderType getType() {
        return PGProviderType.WIREPG;
    }

    @Override
    public PGPaymentResponse pay(PGPaymentRequest request) {
        log.info("[MockPG] 결제 요청: orderId={}, amount={}", request.orderId(), request.amount());

        //        if(request.amount().compareTo(BigDecimal.ZERO) <=0){
        //            return new PGPaymentResponse(false, null, "결제 금액이 유효하지 않습니다.");
        //        }
        //
        //        if(request.amount().compareTo(PAYMENT_LIMIT) > 0){
        //            return new PGPaymentResponse(false, null, "결제 금액이 한도를 초과했습니다.");
        //        }
        //        if ("00000000-0000-0000-0000-000000000000".equals(request.userId().toString())) {
        //            return new PGPaymentResponse(false, null, "카드 승인 거절");
        //        }

        //        String txId = "mock-" + UUID.randomUUID();
        //        log.info("[MockPG] 결제 성공: orderId={}, amount={}, transactionId={}",
        // request.orderId(),
        // request.amount(), txId);
        //        return new PGPaymentResponse(true, txId, "결제 성공 (mock)");
        return null;
    }

    @Override
    public PGPaymentResponse cancel(PGCancelRequest request) {
        log.info("[MockPG] 결제 취소 요청: orderId={}, amount={}", request.orderId(), request.amount());
        return null;
    }
}
