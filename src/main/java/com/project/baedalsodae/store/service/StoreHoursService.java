package com.project.baedalsodae.store.service;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.store.dto.request.StoreHoursRequest;
import com.project.baedalsodae.store.dto.response.StoreHoursResponse;
import java.util.List;
import java.util.UUID;

public interface StoreHoursService {

    void createStoreHours(
            UUID storeId, UserDetailsImpl userDetails, List<StoreHoursRequest> request);

    StoreHoursResponse.StoreHoursInfo getStoreHours(UUID storeId);
