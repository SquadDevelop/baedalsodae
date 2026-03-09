package com.project.baedalsodae.store.controller;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.store.dto.request.store.StoreHoursRequest;
import com.project.baedalsodae.store.dto.response.store.StoreHoursResponse;
import com.project.baedalsodae.store.service.StoreHoursService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/stores/{storeId}/hours")
public class StoreHourController {
    private final StoreHoursService storeHoursService;

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_MANAGER')")
    @PostMapping()
    public ResponseEntity<ApiResponse<Void>> createStoreHours(
            @PathVariable UUID storeId,
            @RequestBody @Valid List<StoreHoursRequest> request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        storeHoursService.createStoreHours(storeId, userDetails, request);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_HOURS_CREATED, null));
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<StoreHoursResponse.StoreHoursInfo>> getStoreHours(
            @PathVariable UUID storeId) {
        StoreHoursResponse.StoreHoursInfo response = storeHoursService.getStoreHours(storeId);
        if (response.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_HOURS_NOT_SET, null));
        }
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_HOURS_FOUND, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_MANAGER')")
    @PatchMapping()
    public ResponseEntity<ApiResponse<Void>> updateStoreHours(
            @PathVariable UUID storeId,
            @RequestBody @Valid List<StoreHoursRequest> request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        storeHoursService.updateStoreHours(storeId, userDetails, request);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_HOURS_UPDATED, null));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'ROLE_MANAGER')")
    @DeleteMapping()
    public ResponseEntity<ApiResponse<Void>> deleteStoreHours(
            @PathVariable UUID storeId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        storeHoursService.deleteStoreHours(storeId, userDetails);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_HOURS_DELETED, null));
    }
}
