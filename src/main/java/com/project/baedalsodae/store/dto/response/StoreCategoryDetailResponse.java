package com.project.baedalsodae.store.dto.response;

import com.project.baedalsodae.global.common.dto.AuditInfoResponse;
import com.project.baedalsodae.store.entity.StoreCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StoreCategoryDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private AuditInfoResponse auditInfo;

    public static StoreCategoryDetailResponse fromStoreCategory(StoreCategory storeCategory) {
        return StoreCategoryDetailResponse.builder()
                .id(storeCategory.getId())
                .name(storeCategory.getName())
                .description(storeCategory.getDescription())
                .auditInfo(AuditInfoResponse.fromEntity(storeCategory))
                .build();
    }
}
