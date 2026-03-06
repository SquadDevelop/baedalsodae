package com.project.baedalsodae.payment.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    CANCELED,
    REFUNDING,
    REFUNDED;

}
