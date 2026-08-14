package com.dinedash.auth.dto;

import com.dinedash.common.security.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72, message = "must be between 8 and 72 characters") String password,
        @NotBlank @Size(max = 150) String fullName,
        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "must be a valid phone number") String phone,
        @NotNull Role role
) {
}
