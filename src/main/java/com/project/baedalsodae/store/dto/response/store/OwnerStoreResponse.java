package com.project.baedalsodae.store.dto.response.store;

import com.project.baedalsodae.global.common.dto.AddressResponse;
import com.project.baedalsodae.store.entity.Store;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerStoreResponse {
    private UUID storeId;
    private String storeName;
    private String phoneNumber;
    private String businessNumber;
    private String description;
    private BigDecimal reviewCount;
    private Double avgRating;
    private String storeStatus;
    private AddressResponse address;

    public static OwnerStoreResponse fromEntity(Store store) {
        return OwnerStoreResponse.builder()
                .storeId(store.getId())
                .storeName(store.getName())
                .phoneNumber(store.getPhone())
                .businessNumber(store.getBusinessNumber())
                .description(store.getDescription())
                .reviewCount(BigDecimal.valueOf(store.getReviewCount()))
                .avgRating(store.getAvgRating())
                .storeStatus(store.getStoreStatus().name())
                .address(AddressResponse.fromEntity(store.getAddress()))
                .build();
    }
}
