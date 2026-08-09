package com.dinedash.common.security;

/**
 * Headers the API gateway adds after validating a JWT.
 * Downstream services read the caller identity from these instead of re-parsing the token.
 */
public final class AuthHeaders {

    public static final String USER_ID = "X-User-Id";
    public static final String USER_EMAIL = "X-User-Email";
    public static final String USER_ROLE = "X-User-Role";

    private AuthHeaders() {
    }
}
