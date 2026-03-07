package com.project.baedalsodae.auth.security;

import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.entity.UserRole;
import io.jsonwebtoken.Claims;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDetailsImpl implements UserDetails {

    private final String username;
    private final String password;

    @Getter private final UUID userId;

    @Getter private final UserRole userRole;

    @Getter private final boolean isDeleted;

    public static UserDetailsImpl from(UUID userId, String username, String password, UserRole userRole, boolean isDeleted) {
        return UserDetailsImpl.builder()
                .userId(userId)
                .username(username)
                .password(password)
                .userRole(userRole)
                .isDeleted(isDeleted)
                .build();
    }

    public static UserDetailsImpl from(Claims claims) {
        return UserDetailsImpl.builder()
                .userId(UUID.fromString(claims.get("userId", String.class)))
                .username(claims.getSubject())
                .password(null)
                .userRole(UserRole.of(claims.get("userRole", String.class)))
                .isDeleted(claims.get("isDeleted", Boolean.class))
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<SimpleGrantedAuthority> grantedAuthority = new ArrayList<>();

        grantedAuthority.add(new SimpleGrantedAuthority(this.userRole.getRole()));

        return grantedAuthority;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isDeleted;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !isDeleted;
    }
}
