package com.project.baedalsodae.auth.controller;

import com.project.baedalsodae.auth.dto.request.LoginRequest;
import com.project.baedalsodae.auth.dto.request.ReissueRequest;
import com.project.baedalsodae.auth.dto.request.SignupRequest;
import com.project.baedalsodae.auth.dto.response.LoginResponse;
import com.project.baedalsodae.auth.dto.response.SignupResponse;
import com.project.baedalsodae.auth.service.AuthService;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.user.dto.response.UserDetailResponse;
import com.project.baedalsodae.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final UserService userService;
    private final AuthService authService;

    @PreAuthorize("permitAll()")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        UserDetailResponse userDetail = userService.createUser(request.toCreateUserDto());

        SignupResponse response =
                SignupResponse.from(
                        userDetail.getUserId(), userDetail.getUsername(), userDetail.getNickname());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.USER_CREATED, response));
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok()
                .header(AUTHORIZATION_HEADER, response.getAccessToken())
                .body(ApiResponse.success(SuccessCode.LOGIN_SUCCESS, response));
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<LoginResponse>> reissue(
            @Valid @RequestBody ReissueRequest request) {
        LoginResponse response = authService.reissue(request.refreshToken());

        return ResponseEntity.ok()
                .header(AUTHORIZATION_HEADER, response.getAccessToken())
                .body(ApiResponse.success(SuccessCode.LOGIN_SUCCESS, response));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(request.getHeader(AUTHORIZATION_HEADER));
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.LOGOUT_SUCCESS.getMessage()));
    }
}
