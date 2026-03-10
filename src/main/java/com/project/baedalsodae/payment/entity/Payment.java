package com.project.baedalsodae.payment.entity;

import com.project.baedalsodae.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "p_payment")
@Getter
public class Payment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    //    @ManyToOne(fetch = FetchType.LAZY)
    //    @JoinColumn(name = "order_id", nullable = false)
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    //    @ManyToOne(fetch = FetchType.LAZY)
    //    @JoinColumn(name = "user_id", nullable = false)
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "pg_transaction_id")
    private String pgTransactionId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    public Payment(
            UUID orderId,
            UUID userId,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            PaymentStatus status,
            LocalDateTime paidAt,
            UUID createdBy,
            UUID updatedBy) {
        validate(orderId, userId, amount, paymentMethod, status, paidAt);
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paidAt = paidAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    private void validate(
            UUID orderId,
            UUID userId,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            PaymentStatus status,
            LocalDateTime paidAt) {
        if (orderId == null) {
            throw new IllegalArgumentException("주문 ID는 null일 수 없습니다.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 null일 수 없습니다.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("결제 수단은 null일 수 없습니다.");
        }
        if (status != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("결제 상태는 PENDING으로 초기화되어야 합니다.");
        }
        if (paidAt != null) {
            throw new IllegalArgumentException("결제 시각은 null로 초기화되어야 합니다.");
        }
    }

    public static Payment create(
            UUID orderId,
            UUID userId,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            PaymentStatus status,
            LocalDateTime paidAt,
            UUID createdBy) {
        return new Payment(
                orderId, userId, amount, paymentMethod, status, paidAt, createdBy, createdBy);
    }

    public void markAsSuccess(final String pgTransactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제 상태는 PENDING이어야 합니다.");
        }
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = LocalDateTime.now();
        this.pgTransactionId = pgTransactionId;
    }

    public void markAsFailed() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제 상태는 PENDING이어야 합니다.");
        }
        this.status = PaymentStatus.FAILED;
    }

    public void markAsCanceled() {
        if (this.status != PaymentStatus.SUCCESS && this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제 상태는 SUCCESS 또는 PENDING 이어야 합니다.");
        }
        this.status = PaymentStatus.CANCELED;
    }
}
