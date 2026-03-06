package com.project.baedalsodae.auth.controller;

import com.project.baedalsodae.auth.dto.request.SignupRequest;
import com.project.baedalsodae.auth.dto.response.SignupResponse;
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

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        UserDetailResponse userDetail = userService.createUser(request.toCreateUserDto());

        SignupResponse response = SignupResponse.from(
                userDetail.getUserId(),
                userDetail.getUsername(),
                userDetail.getNickname()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.USER_CREATED, response));
    }

    @PostMapping("/login")
    public void login() {
        // JwtAuthenticationFilter가 요청을 가로채서 처리하므로 이 코드는 실행되지 않습니다.
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("로그아웃되었습니다."));
    }
}
