package com.project.baedalsodae.store.dto.response;

import com.project.baedalsodae.store.entity.Store;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

@Getter
@Builder
public class StoreSearchPageResponse {
    private List<StoreSummaryResponse> stores;
    private Long totalCount;
    private Integer currentPage;
    private Boolean hasPrevious;
    private Boolean hasNext;

    public static StoreSearchPageResponse of(
            List<Store> stores, Long totalCount, Pageable pageable, Boolean hasNext) {
        return StoreSearchPageResponse.builder()
                .stores(StoreSummaryResponse.fromList(stores))
                .totalCount(totalCount)
                .currentPage(pageable.getPageNumber() + 1)
                .hasNext(hasNext)
                .hasPrevious(pageable.getPageNumber() > 0)
                .build();
    }
}
