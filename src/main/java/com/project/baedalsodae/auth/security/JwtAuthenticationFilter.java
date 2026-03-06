package com.project.baedalsodae.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.dto.request.LoginRequest;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final JwtProvider jwtProvider;
	private final ObjectMapper objectMapper;

	public JwtAuthenticationFilter(JwtProvider jwtProvider) {
		this.jwtProvider = jwtProvider;
		this.objectMapper = new ObjectMapper();
		setFilterProcessesUrl("/api/v1/auth/login");
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		try {
			LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
			AuthenticationManager authManager = getAuthenticationManager();

			return authManager.authenticate(new UsernamePasswordAuthenticationToken(
					loginRequest.username(),
					loginRequest.password(),
					null
			));
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
											FilterChain chain, Authentication authResult) throws IOException, ServletException {
		UserDetailsImpl userDetails = (UserDetailsImpl) authResult.getPrincipal();
		String accessToken = jwtProvider.createAccessToken(
				userDetails.getUserId(),
				userDetails.getUsername(),
				userDetails.getUserRole(),
				userDetails.isDeleted()
		);
		response.addHeader("Authorization", accessToken);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
											  AuthenticationException failed) throws IOException, ServletException {
		sendErrorResponse(response);
	}

	private void sendErrorResponse(HttpServletResponse response) throws IOException {
		response.setStatus(ErrorCode.LOGIN_FAILED.getStatus().value());
		response.setContentType("application/json;charset=UTF=8");
		String json = objectMapper.writeValueAsString(
				ApiResponse.error(ErrorCode.LOGIN_FAILED)
		);
		response.getWriter().write(json);
	}
}
