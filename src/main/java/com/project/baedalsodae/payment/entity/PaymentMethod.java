package com.project.baedalsodae.payment.entity;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CREDIT_CARD("신용카드"),
    DEBIT_CARD("체크카드"),
    MOBILE_PAYMENT("모바일 결제"),
    BANK_TRANSFER("계좌이체"),
    CASH("현금");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }
}
