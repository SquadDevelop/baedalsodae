package com.project.baedalsodae.store.dto.response;

import com.project.baedalsodae.global.common.dto.AddressResponse;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.enums.StoreStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StoreResponse {
    private UUID storeId;
    private String storeName;
    private String phoneNumber;
    private String description;
    private Integer reviewCount;
    private Double avgRating;
    private String storeStatus;
    private StoreStatus status;
    private AddressResponse address;

    // TODO: 가게 운영시간 추가

    public static StoreResponse fromEntity(Store store) {
        return StoreResponse.builder()
                .storeId(store.getId())
                .storeName(store.getName())
                .phoneNumber(store.getPhone())
                .description(store.getDescription())
                .reviewCount(store.getReviewCount())
                .avgRating(store.getAvgRating())
                .storeStatus(store.getStoreStatus().name())
                .address(AddressResponse.fromEntity(store.getAddress()))
                .build();
    }
}
