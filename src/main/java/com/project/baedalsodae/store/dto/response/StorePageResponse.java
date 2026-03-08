package com.project.baedalsodae.store.dto.response;

import com.project.baedalsodae.store.entity.Store;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
@Builder
public class StorePageResponse {
    private UUID storeCategoryId;
    private String storeCategoryName;

    private boolean hasNext;
    private int storeCount;
    private UUID lastCursorId;
    private List<StoreSummaryResponse> stores;

    public static StorePageResponse of(UUID categoryId, String categoryName, Slice<Store> slice) {
        return StorePageResponse.builder()
                .storeCategoryId(categoryId)
                .storeCategoryName(categoryName)
                .hasNext(slice.hasNext())
                .storeCount(slice.getNumberOfElements())
                .lastCursorId(getLastCursorId(slice))
                .stores(StoreSummaryResponse.fromList(slice.getContent()))
                .build();
    }

    private static UUID getLastCursorId(Slice<Store> slice) {
        List<Store> content = slice.getContent();

        if (content.isEmpty()) return null;

        return content.get(content.size() - 1).getId();
    }
}
