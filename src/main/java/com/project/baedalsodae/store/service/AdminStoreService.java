package com.project.baedalsodae.store.service;

import com.project.baedalsodae.store.dto.request.UpdateStoreRequest;
import com.project.baedalsodae.store.entity.enums.StoreStatus;
import java.util.UUID;

public interface AdminStoreService {
    void updateStore(UpdateStoreRequest request, UUID storeId);

    void deleteStore(UUID storeId, UUID userId);

    void updateStoreStatus(UUID storeId, StoreStatus status);
}
