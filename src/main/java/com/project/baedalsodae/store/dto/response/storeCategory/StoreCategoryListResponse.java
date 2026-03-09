package com.project.baedalsodae.store.dto.response.storeCategory;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Getter
@Builder
public class StoreCategoryListResponse {
    private List<StoreCategoryResponse> storeCategoryList;
    private BigDecimal totalCount;

    public static StoreCategoryListResponse fromList(
            List<StoreCategoryResponse> storeCategoryList) {
        return StoreCategoryListResponse.builder()
                .storeCategoryList(storeCategoryList)
                .totalCount(BigDecimal.valueOf(storeCategoryList.size()))
                .build();
    }
}
