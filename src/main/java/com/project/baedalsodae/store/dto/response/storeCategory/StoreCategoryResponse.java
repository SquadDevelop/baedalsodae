package com.project.baedalsodae.store.dto.response.storeCategory;

import com.project.baedalsodae.store.entity.StoreCategory;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StoreCategoryResponse {
    private UUID id;
    private String name;

    public static StoreCategoryResponse fromEntity(StoreCategory storeCategory) {
        return StoreCategoryResponse.builder()
                .id(storeCategory.getId())
                .name(storeCategory.getName())
                .build();
    }
}
