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
import jakarta.persistence.EntityManager;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreHoursServiceImpl implements StoreHoursService {

    private final StoreHoursRepository storeHoursRepository;
    private final StoreRepository storeRepository;
    private final EntityManager entityManager;

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

    private List<StoreHours> getStoreHoursList(List<StoreHoursRequest> requests, Store store) {
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

    @Override
    @Transactional(readOnly = true)
    public StoreHoursResponse.StoreHoursInfo getStoreHours(UUID storeId) {
        checkIfStoreIdValid(storeId);
        List<StoreHours> storeHoursList = storeHoursRepository.findAllByStoreId(storeId);
        List<StoreHoursResponse.StoreHourDto> storeHourDtos =
                StoreHoursResponse.StoreHourDto.fromEntityList(storeHoursList);
        return new StoreHoursResponse.StoreHoursInfo(storeId, storeHourDtos);
    }

    @Transactional
    @Override
    public void updateStoreHours(
            UUID storeId, UserDetailsImpl userDetails, List<StoreHoursRequest> requests) {
        validateDayCount(requests);
        Store store = getStoreByStoreId(storeId);
        StoreOwnershipValidator.verifyStoreOwnership(store, userDetails, ErrorCode.STORE_FORBIDDEN);
        if (!storeHoursRepository.existsByStoreId(storeId)) {
            throw new BusinessException(ErrorCode.STORE_HOURS_NOT_FOUND);
        }
        storeHoursRepository.deleteAllByStoreId(storeId);
        storeHoursRepository.saveAll(getStoreHoursList(requests, store));
    }

    @Transactional
    @Override
    public void deleteStoreHours(UUID storeId, UserDetailsImpl userDetails) {
        Store store = getStoreByStoreId(storeId);
        StoreOwnershipValidator.verifyStoreOwnership(store, userDetails, ErrorCode.STORE_FORBIDDEN);
        bulkDeleteStoreHours(storeId);
    }

    @Override
    public void bulkDeleteStoreHours(UUID storeId) {
        storeHoursRepository.deleteAllByStoreId(storeId);

        entityManager.flush();
        entityManager.clear();
    }

    private void validateDayCount(List<StoreHoursRequest> request) {
        if (request == null || request.isEmpty()) {
            throw new BusinessException(ErrorCode.STORE_HOURS_INVALID_DAY_COUNT);
        }

        if (request.size() != 7) {
            throw new BusinessException(ErrorCode.STORE_HOURS_INVALID_DAY_COUNT);
        }

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

    private void checkIfStoreIdValid(UUID storeId) {
        if (!storeRepository.existsByIdAndIsDeletedIsFalse(storeId)) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
    }
}
