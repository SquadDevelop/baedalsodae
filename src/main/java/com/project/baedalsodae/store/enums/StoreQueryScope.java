package com.project.baedalsodae.store.enums;

import com.project.baedalsodae.user.entity.UserRole;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum StoreQueryScope {
    USER,
    ADMIN;

    public static StoreQueryScope fromRole(UserRole userRole) {
        log.info(String.valueOf(UserRole.CUSTOMER));
        return userRole == UserRole.CUSTOMER ? USER : ADMIN;
    }
}
