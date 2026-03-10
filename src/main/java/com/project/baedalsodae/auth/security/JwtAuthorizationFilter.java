package com.project.baedalsodae.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.security.util.TokenRedisUtil;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final TokenRedisUtil tokenRedisUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String resolvedAccessToken = jwtProvider.resolveToken(request.getHeader("Authorization"));

        if (StringUtils.hasText(resolvedAccessToken)) {
            try {
                if (isBlacklistedSafe(resolvedAccessToken)) {
                    sendErrorResponse(response, ErrorCode.UNAUTHORIZED);
                    return;
                }

                Claims claims = getClaimsForRequest(request, resolvedAccessToken);

                if (!jwtProvider.isAccessToken(claims)) {
                    sendErrorResponse(response, ErrorCode.UNAUTHORIZED);
                    return;
                }

                setAuthentication(claims);
            } catch (BusinessException e) {
                sendErrorResponse(response, e.getErrorCode());
                return;
            } catch (AuthenticationException e) {
                sendErrorResponse(response, ErrorCode.UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isBlacklistedSafe(String token) {
        try {
            return tokenRedisUtil.isBlacklisted(token);
        } catch (Exception e) {
            log.error("Redis error during blacklist check (Fail-Open): {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaimsForRequest(HttpServletRequest request, String token)
            throws BusinessException {
        return isLogoutRequest(request)
                ? jwtProvider.getClaimsIgnoreExpiration(token)
                : jwtProvider.getClaims(token);
    }

    private boolean isLogoutRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String logoutPath = request.getContextPath() + "/auth/logout";
        return HttpMethod.POST.matches(request.getMethod())
                && (uri.equals(logoutPath) || uri.equals(logoutPath + "/"));
    }

    private void setAuthentication(Claims claims) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(claims);

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private Authentication createAuthentication(Claims claims) {
        UserDetails userDetails = UserDetailsImpl.from(claims);
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode)
            throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        String json = objectMapper.writeValueAsString(ApiResponse.error(errorCode));
        response.getWriter().write(json);
    }
}
