package com.project.baedalsodae.store.service.impl;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.menu.common.StoreOwnershipValidator;
import com.project.baedalsodae.store.dto.request.StoreHoursRequest;
import com.project.baedalsodae.store.dto.response.StoreHoursResponse;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.StoreHours;
import com.project.baedalsodae.store.repository.StoreHoursRepository;
import com.project.baedalsodae.store.repository.StoreRepository;
import com.project.baedalsodae.store.service.StoreHoursService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreHoursServiceImpl implements StoreHoursService {

    private final StoreHoursRepository storeHoursRepository;
    private final StoreRepository storeRepository;

    @Transactional
    @Override
    public void createStoreHours(
            UUID storeId, UserDetailsImpl userDetails, List<StoreHoursRequest> requests) {
        validateDayCount(requests);
        Store store = getStoreByStoreId(storeId);
        StoreOwnershipValidator.verifyStoreOwnership(store, userDetails, ErrorCode.STORE_FORBIDDEN);
        if (storeHoursRepository.existsByStoreId(storeId)) {
            throw new BusinessException(ErrorCode.STORE_HOURS_ALREADY_EXISTS);
        }
        List<StoreHours> storeHoursList = getStoreHoursList(requests, store);
        storeHoursRepository.saveAll(storeHoursList);
    }

    private static List<StoreHours> getStoreHoursList(
            List<StoreHoursRequest> requests, Store store) {
        List<StoreHours> storeHoursList = new ArrayList<>();
        for (StoreHoursRequest request : requests) {
            StoreHours storeHours =
                    StoreHours.createStoreHours(
                            store,
                            request.getDayOfWeek(),
                            request.getOpenTime(),
                            request.getCloseTime(),
                            request.getBreakStart(),
                            request.getBreakEnd(),
                            request.isOpen());
            storeHoursList.add(storeHours);
        }
        return storeHoursList;
    }

    private void validateDayCount(List<StoreHoursRequest> request) {
        long distinctDayCount =
                request.stream().map(StoreHoursRequest::getDayOfWeek).distinct().count();
        if (distinctDayCount != 7) {
            throw new BusinessException(ErrorCode.STORE_HOURS_INVALID_DAY_COUNT);
        }
    }

    private Store getStoreByStoreId(UUID storeId) {
        return storeRepository
                .findByIdAndIsDeletedIsFalse(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
    }
}
