package com.project.baedalsodae.allowedRegion.service;

import com.project.baedalsodae.allowedRegion.dto.AllowedRegionRequestDto;
import com.project.baedalsodae.allowedRegion.dto.AllowedRegionResponseDto;
import java.util.UUID;

public interface AllowedRegionService {

    boolean isAllowedByCode(String sigunguCode);

    boolean isAllowedByName(String sigunguName);

    AllowedRegionResponseDto createAllowedRegion(AllowedRegionRequestDto request);

    AllowedRegionResponseDto toggleAllowedRegionActivation(
            UUID allowedRegionId, boolean activation);
}
