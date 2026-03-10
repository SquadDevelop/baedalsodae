package com.project.baedalsodae.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.security.JwtAuthorizationFilter;
import com.project.baedalsodae.auth.security.JwtProvider;
import com.project.baedalsodae.auth.security.util.TokenRedisUtil;
import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.global.common.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class AuthConfig {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final TokenRedisUtil tokenRedisUtil;

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix().role("MASTER").implies("MANAGER").build();
    }

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler =
                new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(jwtProvider, objectMapper, tokenRedisUtil);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(AbstractHttpConfigurer::disable);

        http.sessionManagement(
                (sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(
                (authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers(
                                        PathRequest.toStaticResources().atCommonLocations())
                                .permitAll()
                                .requestMatchers("/actuator/health", "/health")
                                .permitAll()
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/auth/signup",
                                        "/auth/login",
                                        "/auth/reissue")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/auth/logout")
                                .authenticated()
                                .requestMatchers("/users/me")
                                .hasAnyAuthority("ROLE_CUSTOMER", "ROLE_OWNER")
                                .requestMatchers("/admins/me")
                                .hasAuthority("ROLE_MANAGER")
                                .requestMatchers("/admins/**")
                                .hasAnyAuthority("ROLE_MANAGER", "ROLE_MASTER")
                                .requestMatchers("/allowed-regions/**")
                                .hasAnyAuthority("ROLE_MANAGER", "ROLE_MASTER")
                                .requestMatchers("/carts/**")
                                .hasAnyAuthority(
                                        "ROLE_CUSTOMER",
                                        "ROLE_OWNER",
                                        "ROLE_MANAGER",
                                        "ROLE_MASTER")
                                .requestMatchers("/user-addresses/**")
                                .hasAnyAuthority(
                                        "ROLE_CUSTOMER",
                                        "ROLE_OWNER",
                                        "ROLE_MANAGER",
                                        "ROLE_MASTER")
                                .requestMatchers("/menu-categories/**")
                                .hasAnyAuthority(
                                        "ROLE_CUSTOMER",
                                        "ROLE_OWNER",
                                        "ROLE_MANAGER",
                                        "ROLE_MASTER")
                                .requestMatchers("/menu-items/**")
                                .hasAnyAuthority(
                                        "ROLE_CUSTOMER",
                                        "ROLE_OWNER",
                                        "ROLE_MANAGER",
                                        "ROLE_MASTER")
                                .requestMatchers("/orders/**")
                                .hasAnyAuthority(
                                        "ROLE_CUSTOMER",
                                        "ROLE_OWNER",
                                        "ROLE_MANAGER",
                                        "ROLE_MASTER")
                                .requestMatchers("/payments/**")
                                .hasAnyAuthority(
                                        "ROLE_CUSTOMER",
                                        "ROLE_OWNER",
                                        "ROLE_MANAGER",
                                        "ROLE_MASTER")
                                .requestMatchers("/store-categories/**")
                                .hasAnyAuthority(
                                        "ROLE_CUSTOMER",
                                        "ROLE_OWNER",
                                        "ROLE_MANAGER",
                                        "ROLE_MASTER")
                                .requestMatchers("/stores/**/hours")
                                .hasAnyAuthority("ROLE_MANAGER", "ROLE_MASTER")
                                .requestMatchers("/stores/**")
                                .permitAll()
                                .requestMatchers("/tags/**")
                                .hasAnyAuthority(
                                        "ROLE_CUSTOMER",
                                        "ROLE_OWNER",
                                        "ROLE_MANAGER",
                                        "ROLE_MASTER")
                                .anyRequest()
                                .permitAll());

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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
