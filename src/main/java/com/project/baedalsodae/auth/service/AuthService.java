package com.project.baedalsodae.auth.service;

import com.project.baedalsodae.auth.dto.request.LoginRequest;
import com.project.baedalsodae.auth.dto.response.LoginResponse;
import com.project.baedalsodae.auth.security.JwtProvider;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.auth.security.util.TokenRedisUtil;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final TokenRedisUtil tokenRedisUtil;
    private final UserDetailsService userDetailsService;

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.username(), request.password()));

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            return issue(userDetails);
        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
    }

    public void logout(String accessToken) {
        String resolvedAccessToken = jwtProvider.resolveToken(accessToken);

        String username = jwtProvider.getClaims(resolvedAccessToken).getSubject();
        long remainingTime = jwtProvider.getRemainingTime(resolvedAccessToken);

        tokenRedisUtil.deleteRefreshToken(username);
        tokenRedisUtil.saveBlacklist(resolvedAccessToken, remainingTime);
    }

    public LoginResponse reissue(String refreshToken) {
        String username = jwtProvider.getClaims(refreshToken).getSubject();

        if (!tokenRedisUtil.hasValidateRefreshToken(username)
                || !refreshToken.equals(tokenRedisUtil.getRefreshToken(username))) {
            tokenRedisUtil.deleteRefreshToken(username);
            throw new BusinessException(ErrorCode.JWT_INVALID);
        }

        UserDetailsImpl userDetails =
                (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

        return issue(userDetails);
    }

    private LoginResponse issue(UserDetailsImpl userDetails) {
        if (!userDetails.isEnabled()) {
            tokenRedisUtil.deleteRefreshToken(userDetails.getUsername());
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        String newAccessToken = jwtProvider.createAccessToken(userDetails);
        String newRefreshToken = jwtProvider.createRefreshToken(userDetails.getUsername());
        tokenRedisUtil.saveRefreshToken(
                userDetails.getUsername(),
                newRefreshToken,
                jwtProvider.getRemainingTime(newRefreshToken));

        return LoginResponse.from(newAccessToken, newRefreshToken);
    }
}
