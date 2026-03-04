package com.project.baedalsodae.user.entity;

import lombok.Getter;

@Getter
public enum UserRole {
    CUSTOMER("ROLE_CUSTOMER"),
    OWNER("ROLE_OWNER"),
    MANAGER("ROLE_MANAGER"),
    MASTER("ROLE_MASTER");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }
}
