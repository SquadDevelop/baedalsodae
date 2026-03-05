package com.project.baedalsodae.store.entity.enums;

import lombok.Getter;

@Getter
public enum StoreStatus {
    OPEN("영업중"),
    TEMPORARILY_CLOSED("준비중"),
    SUSPENDED("운영중지"),
    PENDING_APPROVAL("승인대기");

    private final String description;

    StoreStatus(String description) {
        this.description = description;
    }
}
