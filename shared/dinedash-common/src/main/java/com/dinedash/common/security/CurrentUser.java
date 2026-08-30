package com.dinedash.common.security;

/**
 * Caller identity as forwarded by the API gateway.
 */
public record CurrentUser(Long id, String email, Role role) {

    public boolean is(Role expected) {
        return role == expected;
    }
}
