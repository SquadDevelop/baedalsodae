package com.project.baedalsodae.payment.service.impl;

import com.project.baedalsodae.event.entity.EventType;
import com.project.baedalsodae.event.publisher.EventPublisher;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.TimeCursorPage;
import com.project.baedalsodae.payment.dto.response.PaymentResponse;
import com.project.baedalsodae.payment.entity.Payment;
import com.project.baedalsodae.payment.entity.PaymentMethod;
import com.project.baedalsodae.payment.entity.PaymentStatus;
import com.project.baedalsodae.payment.pg.dto.PGCancelRequest;
import com.project.baedalsodae.payment.pg.dto.PGPaymentRequest;
import com.project.baedalsodae.payment.pg.dto.PGPaymentResponse;
import com.project.baedalsodae.payment.pg.enums.PGProviderType;
import com.project.baedalsodae.payment.pg.service.PGClient;
import com.project.baedalsodae.payment.repository.PaymentRepository;
import com.project.baedalsodae.payment.service.PaymentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    private final Map<PGProviderType, PGClient> paymentGateways;
    private final EventPublisher eventPublisher;
    private final PaymentRepository paymentRepository;

    @Override
    public TimeCursorPage<List<PaymentResponse>> getPayments(Instant cursor, int size) {
        List<Payment> paymentList = paymentRepository.findNextPage(cursor, size + 1);

        boolean hasNext = paymentList.size() > size;

        List<Payment> contentEntities = hasNext ? paymentList.subList(0, size) : paymentList;

        LocalDateTime nextCursor =
                hasNext
                        ? LocalDateTime.from(
                                contentEntities.get(contentEntities.size() - 1).getCreatedAt())
                        : null;

        List<PaymentResponse> content = paymentList.stream().map(PaymentResponse::from).toList();
        if (hasNext) {
            content = content.subList(0, content.size() - 1);
        }
        return TimeCursorPage.of(content, hasNext, nextCursor);
    }

    @Override
    public PaymentResponse getPayment(final UUID paymentId) {
        Payment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(
                                () -> new BusinessException(ErrorCode.PAYMENT_DETAIL_NOT_FOUND));
        return PaymentResponse.from(payment);
    }

    @Override
    @Transactional
    public void processPayment(
            final UUID orderId, final UUID userId, final BigDecimal finalAmount) {

        // 이미 결제가 생성된 주문 정보인지 확인
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_DONE);
        }

        // pg 설정
        PGClient pgClient = paymentGateways.get(PGProviderType.WIREPG);
        if (pgClient == null) {
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_NOT_FOUND);
        }

        // payment 생성 & 저장
        Payment payment =
                Payment.create(
                        orderId,
                        userId,
                        finalAmount,
                        PaymentMethod.CREDIT_CARD,
                        PaymentStatus.PENDING,
                        null,
                        userId);
        paymentRepository.save(payment);

        // pg에 결제 요청
        PGPaymentRequest pgPaymentRequest =
                new PGPaymentRequest(orderId, userId, PaymentMethod.CREDIT_CARD, finalAmount);
        PGPaymentResponse response = pgClient.pay(pgPaymentRequest);

        // 결제 결과 따라서 payment update
        if (response.status() == PaymentStatus.SUCCESS) {
            payment.markAsSuccess(response.pgTransactionId());
        } else {
            payment.markAsFailed();
        }

        // 주문 도메인에서 결제 결과에 따른 주문 상태 변경 이벤트 발행
        eventPublisher.publishPaymentEvent(payment, EventType.PAYMENT_CREATED);
    }

    @Override
    @Transactional
    public void processPaymentCancel(UUID orderId, UUID userId, BigDecimal finalAmount) {

        Payment foundPayment =
                paymentRepository
                        .findByOrderId(orderId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 이미 취소된 결제인지 확인
        if (foundPayment.getStatus().equals(PaymentStatus.CANCELED)) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_CANCELED);
        }

        // pg 설정
        PGClient pgClient = paymentGateways.get(PGProviderType.WIREPG);
        if (pgClient == null) {
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_NOT_FOUND);
        }

        // pg 취소 요청
        PGCancelRequest pgCancelRequest =
                new PGCancelRequest(
                        orderId,
                        userId,
                        PaymentMethod.CREDIT_CARD,
                        finalAmount,
                        foundPayment.getPgTransactionId());
        PGPaymentResponse pgPaymentResponse = pgClient.cancel(pgCancelRequest);

        // payment status 취소로 업데이트
        foundPayment.markAsCanceled();

        // 주문 도메인에서 결제 결과에 따른 주문 상태 변경 이벤트 발행
        eventPublisher.publishPaymentEvent(foundPayment, EventType.PAYMENT_CREATED);
    }
}
