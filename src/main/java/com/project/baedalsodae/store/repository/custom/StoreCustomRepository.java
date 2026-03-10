package com.project.baedalsodae.store.repository.custom;

import com.project.baedalsodae.store.dto.request.store.StoreCursorRequest;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.enums.SortType;
import com.project.baedalsodae.store.enums.StoreQueryScope;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface StoreCustomRepository {
    Slice<Store> findStoresByCursor(
            UUID storeCategoryId,
            StoreCursorRequest request,
            SortType sortType,
            StoreQueryScope scope);

    List<Store> searchStoreByKeyword(
            String keyword, Pageable pageable, SortType sortType, StoreQueryScope scope);

    Long countStoresByKeyword(String keyword);

    Optional<Store> findByIdAndScope(UUID storeId, StoreQueryScope scope);
}
