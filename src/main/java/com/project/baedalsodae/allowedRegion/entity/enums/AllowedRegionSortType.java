package com.project.baedalsodae.allowedRegion.entity.enums;

public enum AllowedRegionSortType {
    LATEST,
    SIDO_NAME,
    ACTIVE;

    public boolean isAllowedRegionListSort() {
        return this == LATEST || this == SIDO_NAME || this == ACTIVE;
    }
}
