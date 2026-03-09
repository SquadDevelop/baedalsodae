package com.project.baedalsodae.store.dto.response.storeCategory;

import com.project.baedalsodae.global.common.dto.AuditInfoResponse;
import com.project.baedalsodae.store.entity.StoreCategory;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreCategoryDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private AuditInfoResponse auditInfo;

    public static StoreCategoryDetailResponse fromEntity(StoreCategory storeCategory) {
        return StoreCategoryDetailResponse.builder()
                .id(storeCategory.getId())
                .name(storeCategory.getName())
                .description(storeCategory.getDescription())
                .auditInfo(AuditInfoResponse.fromEntity(storeCategory))
                .build();
    }
}
