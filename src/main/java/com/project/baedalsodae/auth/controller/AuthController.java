package com.project.baedalsodae.auth.controller;

import com.project.baedalsodae.auth.dto.request.LoginRequest;
import com.project.baedalsodae.auth.dto.request.SignupRequest;
import com.project.baedalsodae.auth.dto.response.SignupResponse;
import com.project.baedalsodae.auth.service.AuthService;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.SuccessCode;
import com.project.baedalsodae.user.dto.response.UserDetailResponse;
import com.project.baedalsodae.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody LoginRequest request) {
        String accessToken = authService.login(request);

        return ResponseEntity.ok()
                .header("Authorization", accessToken)
                .body(ApiResponse.success(SuccessCode.LOGIN_SUCCESS.getMessage()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.LOGOUT_SUCCESS.getMessage()));
    }
}
