package com.project.baedalsodae.store.repository.custom;

import com.project.baedalsodae.store.dto.request.StoreCursorRequest;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.enums.SortType;
import java.util.UUID;
import org.springframework.data.domain.Slice;

public interface StoreCustomRepository {
    Slice<Store> findStoresByCursor(
            UUID storeCategoryId, StoreCursorRequest request, SortType sortType);
}
