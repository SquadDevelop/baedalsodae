package com.project.baedalsodae.store.dto.response;

import com.project.baedalsodae.store.entity.Store;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StoreSummaryResponse {
    private UUID storeId;
    private String storeName;
    private Integer reviewCount;
    private Double avgRating;
    private String storeStatus;

    public static StoreSummaryResponse fromEntity(Store store) {
        return StoreSummaryResponse.builder()
                .storeId(store.getId())
                .storeName(store.getName())
                .reviewCount(store.getReviewCount())
                .avgRating(store.getAvgRating())
                .storeStatus(store.getStoreStatus().name())
                .build();
    }
}
