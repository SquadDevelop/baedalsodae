package com.project.baedalsodae.store.dto.response;

import com.project.baedalsodae.store.entity.Store;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class StorePageResponse {
    private UUID storeCategoryId;
    private String storeCategoryName;

    private boolean hasNext;
    private int pageSize;
    private List<StoreResponse> stores;

    public static StorePageResponse of(UUID categoryId, String categoryName, Slice<Store> slice) {
        return StorePageResponse.builder()
                .storeCategoryId(categoryId)
                .storeCategoryName(categoryName)
                .hasNext(slice.hasNext())
                .pageSize(slice.getSize())
                .stores(slice.getContent().stream()
                        .map(StoreResponse::fromEntity)
                        .toList())
                .build();
    }

}
