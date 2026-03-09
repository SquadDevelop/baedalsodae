package com.project.baedalsodae.allowedRegion.service.impl;

import com.project.baedalsodae.allowedRegion.entity.AllowedRegion;
import com.project.baedalsodae.allowedRegion.repository.AllowedRegionRepository;
import com.project.baedalsodae.allowedRegion.service.AllowedRegionService;
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
}
