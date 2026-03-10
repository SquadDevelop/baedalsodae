package com.project.baedalsodae.store.enums;

import com.project.baedalsodae.user.entity.UserRole;

public enum StoreQueryScope {
    USER,
    ADMIN;

    public static StoreQueryScope fromRole(UserRole userRole) {
        return userRole == UserRole.CUSTOMER ? USER : ADMIN;
    }
}
