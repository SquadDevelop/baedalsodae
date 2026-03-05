package com.project.baedalsodae.payment.service.impl;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.global.common.TimeCursorPage;
import com.project.baedalsodae.payment.dto.response.PaymentResponse;
import com.project.baedalsodae.payment.entity.Payment;
import com.project.baedalsodae.payment.repository.PaymentRepository;
import com.project.baedalsodae.payment.service.PaymentService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

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
}
