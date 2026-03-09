package com.project.baedalsodae.store.controller;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.store.dto.request.UpdateStoreRequest;
import com.project.baedalsodae.store.dto.request.UpdateStoreStatusRequest;
import com.project.baedalsodae.store.service.AdminStoreService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("admins/stores")
public class AdminStoreController {
    private final AdminStoreService adminStoreService;

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_MASTER')")
    @PatchMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> updateStore(
            @RequestBody @Valid UpdateStoreRequest request, @PathVariable UUID storeId) {
        adminStoreService.updateStore(request, storeId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_UPDATED, null));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_MASTER')")
    @PatchMapping("/{storeId}/status")
    public ResponseEntity<ApiResponse<Void>> updateStoreStatus(
            @RequestBody @Valid UpdateStoreStatusRequest request, @PathVariable UUID storeId) {
        adminStoreService.updateStoreStatus(storeId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_STATUS_UPDATED, null));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_MASTER')")
    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(
            @AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable UUID storeId) {
        adminStoreService.deleteStore(storeId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.STORE_DELETED, null));
    }
}
