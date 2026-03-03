package com.project.baedalsodae.store.dto.response;

import com.project.baedalsodae.store.entity.StoreCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class StoreCategoryResponse {
    private UUID id;
    private String name;

    public static StoreCategoryResponse from(StoreCategory storeCategory) {
        return StoreCategoryResponse.builder()
                .id(storeCategory.getId())
                .name(storeCategory.getName())
                .build();
    }
}
