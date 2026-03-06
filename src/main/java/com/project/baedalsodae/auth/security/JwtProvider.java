package com.project.baedalsodae.auth.security;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.user.entity.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtProvider {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private static final String USER_ID = "userId";
	private static final String USER_ROLE = "userRole";
	private static final String IS_DELETED = "isDeleted";

	private static final long ACCESS_TOKEN_TIME = 30*60*1000L;

	@Value("${JWT_SECRET_KEY}")
	private String secretKey;
	private SecretKey jwtSecretKey;

	@PostConstruct
	public void init() {
		this.jwtSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
	}

	public String createAccessToken(UUID id, String username, UserRole role, boolean isDeleted) {
		Date now = new Date();
		Date expiration = new Date(now.getTime() + ACCESS_TOKEN_TIME);
		String newAccessToken = Jwts.builder()
				.subject(username)
				.claim(USER_ID, id.toString())
				.claim(USER_ROLE, role.getRole())
				.claim(IS_DELETED, isDeleted)
				.expiration(expiration)
				.issuedAt(now)
				.signWith(jwtSecretKey, Jwts.SIG.HS256)
				.compact();

		return String.format("%s%s", BEARER_PREFIX, newAccessToken);
	}

	public String resolveToken(HttpServletRequest request) {
		String token = request.getHeader(AUTHORIZATION_HEADER);

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
}
