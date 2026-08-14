package com.dinedash.auth.security;

import com.dinedash.common.security.Role;

/**
 * Principal stored in the security context once a bearer token is verified.
 */
public record AuthenticatedUser(Long id, String email, Role role) {
}
