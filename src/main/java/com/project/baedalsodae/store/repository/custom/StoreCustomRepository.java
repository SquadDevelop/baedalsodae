package com.project.baedalsodae.store.repository.custom;

import com.project.baedalsodae.store.dto.request.StoreCursorRequest;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.enums.SortType;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface StoreCustomRepository {
    Slice<Store> findStoresByCursor(
            UUID storeCategoryId, StoreCursorRequest request, SortType sortType);

    List<Store> searchStoreByKeyword(String keyword, Pageable pageable, SortType sortType);
    Long countStoresByKeyword(String keyword);
}
