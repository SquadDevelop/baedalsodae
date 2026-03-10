package com.project.baedalsodae.auth.security;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class JwtProvider {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String USER_ID = "userId";
    private static final String USER_ROLE = "userRole";
    private static final String IS_DELETED = "isDeleted";
    private static final String TOKEN_TYPE = "tokenType";
    private static final String ACCESS_TYPE = "ACCESS";
    private static final String REFRESH_TYPE = "REFRESH";

    @Value("${JWT_SECRET}")
    private String secretKey;

    @Value("${JWT_EXPIRATION}")
    private long jwtExpiration;

    @Value("${JWT_REFRESH_EXPIRATION}")
    private long jwtRefreshExpiration;

    private SecretKey jwtSecretKey;

    @PostConstruct
    public void init() {
        this.jwtSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String createAccessToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtExpiration);
        String newAccessToken =
                Jwts.builder()
                        .subject(userDetails.getUsername())
                        .claim(USER_ID, ((UserDetailsImpl) userDetails).getUserId().toString())
                        .claim(USER_ROLE, ((UserDetailsImpl) userDetails).getUserRole().getRole())
                        .claim(IS_DELETED, ((UserDetailsImpl) userDetails).isDeleted())
                        .claim(TOKEN_TYPE, ACCESS_TYPE)
                        .expiration(expiration)
                        .issuedAt(now)
                        .signWith(jwtSecretKey, Jwts.SIG.HS256)
                        .compact();

        return String.format("%s%s", BEARER_PREFIX, newAccessToken);
    }

    public String createRefreshToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtRefreshExpiration);

        return Jwts.builder()
                .subject(username)
                .claim(TOKEN_TYPE, REFRESH_TYPE)
                .expiration(expiration)
                .issuedAt(now)
                .signWith(jwtSecretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String resolveToken(String token) {
        if (!StringUtils.hasText(token) || !token.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return token.substring(BEARER_PREFIX.length());
    }

    public Claims getClaims(String inputToken) {
        try {
            return Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(inputToken)
                    .getPayload();
        } catch (SecurityException | MalformedJwtException e) {
            throw new BusinessException(ErrorCode.JWT_SIGNATURE_INVALID);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.JWT_EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new BusinessException(ErrorCode.JWT_UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.JWT_INVALID);
        }
    }

    public Claims getClaimsIgnoreExpiration(String inputToken) {
        try {
            return Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(inputToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (SecurityException | MalformedJwtException e) {
            throw new BusinessException(ErrorCode.JWT_SIGNATURE_INVALID);
        } catch (UnsupportedJwtException e) {
            throw new BusinessException(ErrorCode.JWT_UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.JWT_INVALID);
        }
    }

    public long getRemainingTime(String token) {
        Claims claims = this.getClaims(token);
        Date expiration = claims.getExpiration();
        long now = new Date().getTime();

        return Math.max(0, expiration.getTime() - now);
    }

    public long getRemainingTimeSafe(String token) {
        try {
            return getRemainingTime(token);
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.JWT_EXPIRED) {
                return 0;
            }
            throw e;
        }
    }

    public boolean isAccessToken(Claims claims) {
        return ACCESS_TYPE.equals(claims.get(TOKEN_TYPE));
    }

    public boolean isRefreshToken(Claims claims) {
        return REFRESH_TYPE.equals(claims.get(TOKEN_TYPE));
    }
}
