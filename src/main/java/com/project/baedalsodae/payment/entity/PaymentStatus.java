package com.project.baedalsodae.payment.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("결제 대기"),
    SUCCESS("결제 완료"),
    FAILED("결제 실패"),
    CANCELED("결제 취소"),
    REFUNDING("환불 진행 중"),
    REFUNDED("환불 완료");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }
}
