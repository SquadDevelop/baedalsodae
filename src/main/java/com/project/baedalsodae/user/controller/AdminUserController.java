package com.project.baedalsodae.user.controller;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.user.dto.request.CreateUserRequest;
import com.project.baedalsodae.user.dto.request.UserSearchRequest;
import com.project.baedalsodae.user.dto.response.AdminUserDetailResponse;
import com.project.baedalsodae.user.dto.response.UserDetailResponse;
import com.project.baedalsodae.user.entity.UserRole;
import com.project.baedalsodae.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admins")
public class AdminUserController {

    private final UserService userService;

    @PostMapping("/managers")
    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    public ApiResponse<AdminUserDetailResponse> createManager(
            @RequestBody @Valid CreateUserRequest createRequest) {
        UserDetailResponse response = userService.createUser(createRequest);
        return ApiResponse.success(
                SuccessCode.USER_CREATED, AdminUserDetailResponse.from(response));
    }

    @GetMapping("/managers")
    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    public ApiResponse<Page<AdminUserDetailResponse>> getManagers(
            UserSearchRequest searchRequest,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {

        Page<UserDetailResponse> managers =
                userService.getUsers(UserRole.MANAGER, searchRequest, pageable);
        Page<AdminUserDetailResponse> response = managers.map(AdminUserDetailResponse::from);

        return ApiResponse.success(SuccessCode.USER_FOUND, response);
    }

    @GetMapping("/managers/{managerId}")
    @PreAuthorize("hasAuthority('ROLE_MASTER')")
    public ApiResponse<AdminUserDetailResponse> getManagerDetail(
            @PathVariable("managerId") UUID userId) {
        UserDetailResponse response = userService.getUser(userId);

        return ApiResponse.success(SuccessCode.USER_FOUND, AdminUserDetailResponse.from(response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ApiResponse<AdminUserDetailResponse> getAdminDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserDetailResponse response = userService.getUser(userDetails.getUserId());

        return ApiResponse.success(SuccessCode.USER_FOUND, AdminUserDetailResponse.from(response));
    }
}
