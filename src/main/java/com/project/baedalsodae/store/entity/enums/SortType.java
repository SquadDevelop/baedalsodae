package com.project.baedalsodae.store.entity.enums;

public enum SortType {
    LATEST,
    RATING,
    REVIEW;

    public boolean isStoreListSort() {
        return this == LATEST || this == RATING || this == REVIEW;
    }
}
