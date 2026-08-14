package com.dinedash.auth.dto;

import com.dinedash.auth.domain.User;
import com.dinedash.common.security.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        Role role,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(),
                user.getPhone(), user.getRole(), user.getCreatedAt());
    }
}
