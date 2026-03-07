package com.project.baedalsodae.menu.common;

import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.user.entity.UserRole;
import java.util.Objects;

public class StoreOwnershipValidator {

    private StoreOwnershipValidator() {
        throw new IllegalStateException("Utility class");
    }

    public static void verifyStoreOwnership(
            Store store, UserDetailsImpl userDetails, ErrorCode errorCode) {
        Objects.requireNonNull(store, "Store must not be null");
        Objects.requireNonNull(userDetails, "UserDetails must not be null");
        Objects.requireNonNull(errorCode, "ErrorCode must not be null");

        if (userDetails.getUserRole() == UserRole.OWNER
                && !store.getUserId().equals(userDetails.getUserId())) {
            throw new BusinessException(errorCode);
        }
    }
}
