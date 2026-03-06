package com.project.baedalsodae.user.controller;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.user.dto.request.UpdateUserRequest;
import com.project.baedalsodae.user.dto.response.UserDeleteResponse;
import com.project.baedalsodae.user.dto.response.UserDetailResponse;
import com.project.baedalsodae.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserDetailResponse response = userService.getUser(userDetails.getUserId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.USER_FOUND, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER')")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateUser(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserDetailResponse response = userService.updateUser(userDetails.getUserId(), request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.USER_UPDATED, response));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_OWNER')")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<UserDeleteResponse>> deleteUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserDeleteResponse response = userService.deleteUser(userDetails.getUserId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.USER_DELETED, response));
    }
}
