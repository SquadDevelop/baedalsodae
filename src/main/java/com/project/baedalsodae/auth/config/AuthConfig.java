package com.project.baedalsodae.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.security.JwtAuthorizationFilter;
import com.project.baedalsodae.auth.security.JwtProvider;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class AuthConfig {

    private final UserDetailsService userDetailsService;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(jwtProvider, userDetailsService, objectMapper);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http.sessionManagement(
                (sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(
                (authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers(
                                        PathRequest.toStaticResources().atCommonLocations()).permitAll()
                                .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                                .requestMatchers("/user/me").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_OWNER")
                                .anyRequest().permitAll()
//                                .anyRequest().authenticated()
                );

        http.exceptionHandling(
                exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(
                                        (request, response, authException) ->
                                                sendErrorResponse(response, ErrorCode.UNAUTHORIZED))
                                .accessDeniedHandler(
                                        (request, response, accessDeniedException) ->
                                                sendErrorResponse(response, ErrorCode.FORBIDDEN)));

        http.addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode)
            throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        String json = objectMapper.writeValueAsString(ApiResponse.error(errorCode));
        response.getWriter().write(json);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
