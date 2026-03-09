package com.project.baedalsodae.allowedRegion.service.impl;

import com.project.baedalsodae.allowedRegion.dto.AllowedRegionCursorRequest;
import com.project.baedalsodae.allowedRegion.dto.AllowedRegionPageResponse;
import com.project.baedalsodae.allowedRegion.dto.AllowedRegionRequestDto;
import com.project.baedalsodae.allowedRegion.dto.AllowedRegionResponseDto;
import com.project.baedalsodae.allowedRegion.entity.AllowedRegion;
import com.project.baedalsodae.allowedRegion.repository.AllowedRegionRepository;
import com.project.baedalsodae.allowedRegion.service.AllowedRegionService;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AllowedRegionServiceImpl implements AllowedRegionService {

    private final AllowedRegionRepository allowedRegionRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isAllowedByCode(String sigunguCode) {
        return allowedRegionRepository
                .findBySigunguCodeAndIsDeletedIsFalse(sigunguCode)
                .map(AllowedRegion::isActive)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAllowedByName(String sigunguName) {
        return allowedRegionRepository
                .findBySigunguNameAndIsDeletedIsFalse(sigunguName)
                .map(AllowedRegion::isActive)
                .orElse(false);
    }

    @Override
    @Transactional
    public AllowedRegionResponseDto createAllowedRegion(AllowedRegionRequestDto request) {
        if (allowedRegionRepository.existsBySigunguCodeAndIsDeletedIsFalse(request.sigunguCode())) {
            throw new BusinessException(ErrorCode.ALLOWED_REGION_CODE_DUPLICATED);
        }
        AllowedRegion allowedRegion =
                AllowedRegion.create(
                        request.sidoCode(),
                        request.sidoName(),
                        request.sigunguCode(),
                        request.sigunguName());
        allowedRegionRepository.save(allowedRegion);
        return AllowedRegionResponseDto.fromEntity(allowedRegion);
    }

    @Override
    @Transactional
    public AllowedRegionResponseDto toggleAllowedRegionActivation(
            UUID allowedRegionId, boolean activation) {
        AllowedRegion allowedRegion =
                allowedRegionRepository
                        .findByIdAndIsDeletedIsFalse(allowedRegionId)
                        .orElseThrow(
                                () -> new BusinessException(ErrorCode.ALLOWED_REGION_NOT_FOUND));
        if (activation) allowedRegion.activate();
        else allowedRegion.deactivate();
        return AllowedRegionResponseDto.fromEntity(allowedRegion);
    }

    @Override
    @Transactional(readOnly = true)
    public AllowedRegionPageResponse getAllowedRegions(AllowedRegionCursorRequest cursorRequest) {
        AllowedRegionCursorRequest cursor = cursorRequest.initCursor(cursorRequest.sortType());
        return AllowedRegionPageResponse.of(
                allowedRegionRepository.findAllowedRegionsByCursor(cursor));
    }
}
