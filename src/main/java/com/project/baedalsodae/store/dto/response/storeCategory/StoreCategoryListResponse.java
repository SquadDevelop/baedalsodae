package com.project.baedalsodae.store.dto.response.storeCategory;

import java.util.List;
import lombok.*;

@Getter
@Builder
public class StoreCategoryListResponse {
    private List<StoreCategoryResponse> storeCategoryList;
    private int totalCount;

    public static StoreCategoryListResponse fromList(
            List<StoreCategoryResponse> storeCategoryList) {
        return StoreCategoryListResponse.builder()
                .storeCategoryList(storeCategoryList)
                .totalCount(storeCategoryList.size())
                .build();
    }
}
