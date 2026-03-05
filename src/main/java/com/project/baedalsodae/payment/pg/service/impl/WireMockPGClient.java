package com.project.baedalsodae.payment.pg.service.impl;

import com.project.baedalsodae.payment.pg.dto.PGCancelRequest;
import com.project.baedalsodae.payment.pg.dto.PGPaymentRequest;
import com.project.baedalsodae.payment.pg.dto.PGPaymentResponse;
import com.project.baedalsodae.payment.pg.service.PGClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Primary
@Component
public class WireMockPGClient implements PGClient {

    private final RestClient restClient;

    public WireMockPGClient(@Value("${pg.url}") String pgUrl) {
        this.restClient = RestClient.builder().baseUrl(pgUrl).build();
    }

    @Override
    public PGPaymentResponse pay(PGPaymentRequest request) {
        log.info(
                "[WireMockPG] 결제 요청: orderId={}, amount={}, method={}",
                request.orderId(),
                request.amount(),
                request.paymentMethod());

        PGPaymentResponse response =
                restClient
                        .post()
                        .uri("/pg/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(PGPaymentResponse.class);

        log.info(
                "[WireMockPG] 결제 응답: success={}, pgTransactionId={}, status={}",
                response != null ? response.success() : null,
                response != null ? response.pgTransactionId() : null,
                response != null ? response.status() : null);

        return response;
    }

    @Override
    public PGPaymentResponse cancel(PGCancelRequest request) {
        log.info(
                "[WireMockPG] 결제 취소 요청: orderId={}, pgTransactionId={}",
                request.orderId(),
                request.pgTransactionId());

        PGPaymentResponse response =
                restClient
                        .post()
                        .uri("/pg/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(PGPaymentResponse.class);

        log.info(
                "[WireMockPG] 결제 취소 응답: success={}, status={}",
                response != null ? response.success() : null,
                response != null ? response.status() : null);

        return response;
    }
}
