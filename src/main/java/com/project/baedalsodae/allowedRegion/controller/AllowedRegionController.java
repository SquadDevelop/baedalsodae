package com.project.baedalsodae.allowedRegion.controller;

import com.project.baedalsodae.allowedRegion.dto.AllowedRegionCursorRequest;
import com.project.baedalsodae.allowedRegion.dto.AllowedRegionPageResponse;
import com.project.baedalsodae.allowedRegion.dto.AllowedRegionRequestDto;
import com.project.baedalsodae.allowedRegion.dto.AllowedRegionResponseDto;
import com.project.baedalsodae.allowedRegion.service.AllowedRegionService;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/allowed-regions")
@RequiredArgsConstructor
public class AllowedRegionController {

    private final AllowedRegionService allowedRegionService;


    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<AllowedRegionResponseDto>> createAllowedRegion(
            @RequestBody AllowedRegionRequestDto request) {
        AllowedRegionResponseDto response = allowedRegionService.createAllowedRegion(request);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ALLOWED_REGION_CREATED, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    @PatchMapping("/{allowedRegionId}/activation")
    public ResponseEntity<ApiResponse<AllowedRegionResponseDto>> toggleAllowedRegion(
            @PathVariable UUID allowedRegionId, @RequestParam boolean activation) {
        AllowedRegionResponseDto response =
                allowedRegionService.toggleAllowedRegionActivation(allowedRegionId, activation);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.ALLOWED_REGION_ACTIVATION_TOGGLED, response));
    }
}
