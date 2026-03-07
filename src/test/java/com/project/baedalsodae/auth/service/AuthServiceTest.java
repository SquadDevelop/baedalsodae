package com.project.baedalsodae.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.project.baedalsodae.auth.dto.request.LoginRequest;
import com.project.baedalsodae.auth.dto.response.LoginResponse;
import com.project.baedalsodae.auth.security.JwtProvider;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.auth.security.util.TokenRedisUtil;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.user.entity.UserRole;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks private AuthService authService;

    @Mock private AuthenticationManager authenticationManager;

    @Mock private JwtProvider jwtProvider;

    @Mock private TokenRedisUtil tokenRedisUtil;

    @Mock private UserDetailsService userDetailsService;

    @Nested
    @DisplayName("로그인 테스트")
    class Login {

        @Test
        @DisplayName("성공 - 올바른 정보로 로그인하면 토큰을 발급하고 Redis에 저장")
        void login_Success() {
            // given
            String username = "tester123";
            LoginRequest request = new LoginRequest(username, "Password123!");
            UUID userId = UUID.randomUUID();

            Authentication mockAuthentication = mock(Authentication.class);
            UserDetailsImpl mockUserDetails = UserDetailsImpl.from(userId, username, null, UserRole.CUSTOMER, false);

            given(authenticationManager.authenticate(any())).willReturn(mockAuthentication);
            given(mockAuthentication.getPrincipal()).willReturn(mockUserDetails);
            given(jwtProvider.createAccessToken(any())).willReturn("Bearer access-token");
            given(jwtProvider.createRefreshToken(anyString())).willReturn("refresh-token");
            given(jwtProvider.getRemainingTime(anyString())).willReturn(604800000L);

            // when
            LoginResponse response = authService.login(request);

            // then
            assertThat(response.getAccessToken()).isEqualTo("Bearer access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");

            // 핵심 검증: Redis 저장 메서드가 호출되었는지 확인
            verify(tokenRedisUtil, times(1)).saveRefreshToken(eq(username), eq("refresh-token"), anyLong());
        }

        @Test
        @DisplayName("실패 - 잘못된 비밀번호로 로그인 시 BusinessException이 발생")
        void login_Fail_BadCredentials() {
            // given
            LoginRequest request = new LoginRequest("tester123", "wrong-password");
            given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("Invalid password"));

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class Logout {

        @Test
        @DisplayName("성공 - 로그아웃 시 Redis에서 Refresh Token을 삭제하고 Access Token을 블랙리스트에 등록")
        void logout_Success() {
            // given
            String accessToken = "Bearer access-token";
            String resolvedAccessToken = "access-token";
            String username = "tester123";
            long remainingTime = 1800000L; // 30분

            Claims mockClaims = mock(Claims.class);

            given(jwtProvider.resolveToken(accessToken)).willReturn(resolvedAccessToken);
            given(jwtProvider.getClaims(resolvedAccessToken)).willReturn(mockClaims);
            given(mockClaims.getSubject()).willReturn(username);
            given(jwtProvider.getRemainingTime(resolvedAccessToken)).willReturn(remainingTime);

            // when
            authService.logout(accessToken);

            // then
            verify(tokenRedisUtil, times(1)).deleteRefreshToken(username);
            verify(tokenRedisUtil, times(1)).saveBlacklist(resolvedAccessToken, remainingTime);
        }
    }

    @Nested
    @DisplayName("토큰 재발급 테스트")
    class Reissue {

        @Test
        @DisplayName("성공 - 유효한 리프레시 토큰으로 재발급 시 새로운 토큰 세트를 반환하고 Redis를 갱신한다")
        void reissue_Success() {
            // given
            String oldRefreshToken = "old-refresh-token";
            String username = "tester123";

            Claims mockClaims = mock(Claims.class);
            UserDetailsImpl mockUserDetails = UserDetailsImpl.from(UUID.randomUUID(), username, null, UserRole.CUSTOMER, false);

            given(jwtProvider.getClaims(oldRefreshToken)).willReturn(mockClaims);
            given(mockClaims.getSubject()).willReturn(username);
            given(tokenRedisUtil.hasValidateRefreshToken(username)).willReturn(true);
            given(tokenRedisUtil.getRefreshToken(username)).willReturn(oldRefreshToken);
            given(userDetailsService.loadUserByUsername(username)).willReturn(mockUserDetails);

            given(jwtProvider.createAccessToken(any())).willReturn("Bearer new-access-token");
            given(jwtProvider.createRefreshToken(username)).willReturn("new-refresh-token");
            given(jwtProvider.getRemainingTime("new-refresh-token")).willReturn(604800000L);

            // when
            LoginResponse response = authService.reissue(oldRefreshToken);

            // then
            assertThat(response.getAccessToken()).isEqualTo("Bearer new-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");

            verify(tokenRedisUtil, times(1)).saveRefreshToken(eq(username), eq("new-refresh-token"), anyLong());
        }

        @Test
        @DisplayName("실패 - Redis에 저장된 토큰과 일치하지 않으면 세션을 삭제하고 예외를 던진다")
        void reissue_Fail_TokenMismatch() {
            // given
            String stolenRefreshToken = "stolen-token";
            String username = "tester123";
            Claims mockClaims = mock(Claims.class);

            given(jwtProvider.getClaims(stolenRefreshToken)).willReturn(mockClaims);
            given(mockClaims.getSubject()).willReturn(username);
            given(tokenRedisUtil.hasValidateRefreshToken(username)).willReturn(true);
            given(tokenRedisUtil.getRefreshToken(username)).willReturn("different-token");

            // when & then
            assertThatThrownBy(() -> authService.reissue(stolenRefreshToken))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JWT_INVALID);

            verify(tokenRedisUtil, times(1)).deleteRefreshToken(username);
        }

        @Test
        @DisplayName("실패 - 계정이 비활성화된 상태에서 재발급 시도 시 세션을 삭제하고 예외를 던진다")
        void reissue_Fail_UserDisabled() {
            // given
            String refreshToken = "valid-token";
            String username = "tester123";
            Claims mockClaims = mock(Claims.class);
            UserDetailsImpl disabledUser = UserDetailsImpl.from(UUID.randomUUID(), username, null, UserRole.CUSTOMER, true);

            given(jwtProvider.getClaims(refreshToken)).willReturn(mockClaims);
            given(mockClaims.getSubject()).willReturn(username);
            given(tokenRedisUtil.hasValidateRefreshToken(username)).willReturn(true);
            given(tokenRedisUtil.getRefreshToken(username)).willReturn(refreshToken);
            given(userDetailsService.loadUserByUsername(username)).willReturn(disabledUser);

            // when & then
            assertThatThrownBy(() -> authService.reissue(refreshToken))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            verify(tokenRedisUtil, times(1)).deleteRefreshToken(username);
        }
    }
}
