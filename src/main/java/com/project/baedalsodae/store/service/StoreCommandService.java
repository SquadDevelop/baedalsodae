package com.project.baedalsodae.store.service;

import com.project.baedalsodae.store.dto.request.CreateStoreRequest;
import com.project.baedalsodae.store.dto.request.UpdateStoreRequest;
import com.project.baedalsodae.store.entity.enums.StoreStatus;

import java.util.UUID;

public interface StoreCommandService {
    void createStore(CreateStoreRequest request, UUID userId);
    void updateStore(UpdateStoreRequest request, UUID storeId, UUID userId);
    void deleteStore(UUID storeId, UUID userId);
    void updateStoreOpened(UUID storeId, StoreStatus status, UUID userId);
}
