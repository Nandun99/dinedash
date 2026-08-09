package com.dinedash.common.security;

/**
 * Platform roles. A user has exactly one role.
 */
public enum Role {
    CUSTOMER,
    RESTAURANT_OWNER,
    DRIVER,
    ADMIN;

    /** Roles a user may pick when signing up. Admins are provisioned separately. */
    public boolean isSelfRegistrable() {
        return this != ADMIN;
    }
}
