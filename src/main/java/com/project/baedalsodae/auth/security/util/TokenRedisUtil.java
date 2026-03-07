package com.project.baedalsodae.auth.security.util;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenRedisUtil {

    private static final String REFRESH_TOKEN_PREFIX = "RefreshToken:";
    private static final String BLACKLIST_PREFIX = "Blacklist:";

    private final StringRedisTemplate redisTemplate;

    public void saveRefreshToken(String username, String refreshToken, long expirationMillis) {
        redisTemplate
                .opsForValue()
                .set(
                        REFRESH_TOKEN_PREFIX + username,
                        refreshToken,
                        expirationMillis,
                        TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + username);
    }

    public void deleteRefreshToken(String username) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + username);
    }

    public boolean hasValidateRefreshToken(String username) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + username));
    }

    public void saveBlacklist(String accessToken, long expirationMillis) {
        redisTemplate
                .opsForValue()
                .set(
                        BLACKLIST_PREFIX + accessToken,
                        "logout",
                        expirationMillis,
                        TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }
}
