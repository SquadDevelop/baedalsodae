package com.project.baedalsodae.payment.pg.service.impl;

import com.project.baedalsodae.payment.pg.dto.PGCancelRequest;
import com.project.baedalsodae.payment.pg.dto.PGPaymentRequest;
import com.project.baedalsodae.payment.pg.dto.PGPaymentResponse;
import com.project.baedalsodae.payment.pg.enums.PGProviderType;
import com.project.baedalsodae.payment.pg.service.PGClient;
import java.net.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Primary
@Component
public class WireMockPGClient implements PGClient {

    private final RestClient restClient;

    public WireMockPGClient(@Value("${pg.url}") String pgUrl) {
        log.info(">>>>> pgUrl {} ", pgUrl);
        HttpClient httpClient =
                HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

        this.restClient =
                RestClient.builder()
                        .baseUrl(pgUrl)
                        .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                        .build();

        log.info(">>>>> pgUrl {} ", httpClient);
    }

    @Override
    public PGProviderType getType() {
        return PGProviderType.WIREPG;
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
                        .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                        .body(request)
                        .retrieve()
                        .body(PGPaymentResponse.class);

        log.info(
                "[WireMockPG] 결제 응답: success={}, pgTransactionId={}, status={}",
                response != null ? response.isSuccess() : null,
                response != null ? response.getPgTransactionId() : null,
                response != null ? response.getStatus() : null);

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
                        .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                        .body(request)
                        .retrieve()
                        .body(PGPaymentResponse.class);

        log.info(
                "[WireMockPG] 결제 취소 응답: success={}, status={}",
                response != null ? response.isSuccess() : null,
                response != null ? response.getStatus() : null);

        return response;
    }
}
