package com.project.baedalsodae.payment.pg.dto;

import com.project.baedalsodae.payment.entity.PaymentStatus;
import jakarta.persistence.EntityListeners;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
public record PGPaymentResponse(
        boolean success,
        String paymentId,
        String pgTransactionId,
        BigDecimal amount,
        PaymentStatus status,
        String message,
        @CreationTimestamp LocalDateTime createdAt,
        @LastModifiedDate LocalDateTime modifiedAt) {}
