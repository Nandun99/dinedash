package com.dinedash.auth.service;

import com.dinedash.auth.domain.User;
import com.dinedash.auth.dto.AuthResponse;
import com.dinedash.auth.dto.LoginRequest;
import com.dinedash.auth.dto.RegisterRequest;
import com.dinedash.auth.dto.UserResponse;
import com.dinedash.auth.exception.BadRequestException;
import com.dinedash.auth.exception.ConflictException;
import com.dinedash.auth.exception.UnauthorizedException;
import com.dinedash.auth.repository.UserRepository;
import com.dinedash.auth.security.JwtService;
import com.dinedash.auth.security.RefreshTokenService;
import com.dinedash.common.security.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerNormalisesEmailAndHashesPassword() {
        when(userRepository.existsByEmailIgnoreCase("nimal@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = authService.register(
                new RegisterRequest("  Nimal@Example.com ", "secret123", "Nimal Silva", null, Role.CUSTOMER));

        assertThat(response.email()).isEqualTo("nimal@example.com");
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("nimal@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("nimal@example.com", "secret123", "Nimal", null, Role.CUSTOMER)))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsAdminRole() {
        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("boss@example.com", "secret123", "Boss", null, Role.ADMIN)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void loginReturnsTokensForValidCredentials() {
        User user = new User("nimal@example.com", "hashed", "Nimal Silva", null, Role.CUSTOMER);
        user.setId(1L);
        when(userRepository.findByEmailIgnoreCase("nimal@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access");
        when(jwtService.accessTokenTtlSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(user)).thenReturn("refresh");

        AuthResponse response = authService.login(new LoginRequest("nimal@example.com", "secret123"));

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        assertThat(response.expiresIn()).isEqualTo(900L);
    }

    @Test
    void loginFailsWithWrongPassword() {
        User user = new User("nimal@example.com", "hashed", "Nimal Silva", null, Role.CUSTOMER);
        when(userRepository.findByEmailIgnoreCase("nimal@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("nimal@example.com", "wrong")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void loginFailsForDisabledAccount() {
        User user = new User("nimal@example.com", "hashed", "Nimal Silva", null, Role.CUSTOMER);
        user.setEnabled(false);
        when(userRepository.findByEmailIgnoreCase("nimal@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("nimal@example.com", "secret123")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("disabled");
    }
}
