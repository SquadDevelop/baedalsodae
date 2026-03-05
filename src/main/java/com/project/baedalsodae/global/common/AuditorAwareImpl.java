package com.project.baedalsodae.global.common;

import java.util.Optional;
import java.util.UUID;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuditorAwareImpl implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetailsImpl)
                .map(auth -> ((UserDetailsImpl) auth.getPrincipal()).getUserId());
    }
}
