package com.project.baedalsodae.user.entity;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum UserRole {
    CUSTOMER("ROLE_CUSTOMER"),
    OWNER("ROLE_OWNER"),
    MANAGER("ROLE_MANAGER"),
    MASTER("ROLE_MASTER");

    private static final Map<String, UserRole> ROLE_MAP =
            Stream.of(UserRole.values())
                    .collect(Collectors.toMap(UserRole::getRole, Function.identity()));

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public static UserRole of(String roleName) {
        return Optional.ofNullable(ROLE_MAP.get(roleName))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_ROLE_NOT_FOUND));
    }
}
