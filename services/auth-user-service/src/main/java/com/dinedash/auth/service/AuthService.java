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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (!request.role().isSelfRegistrable()) {
            throw new BadRequestException("Role " + request.role() + " cannot be self-registered");
        }
        String email = normalize(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = new User(email, passwordEncoder.encode(request.password()),
                request.fullName().trim(), request.phone(), request.role());
        User saved = userRepository.save(user);
        log.info("Registered user id={} role={}", saved.getId(), saved.getRole());
        return UserResponse.from(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalize(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        return issueTokens(user);
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public AuthResponse refresh(String refreshToken) {
        return issueTokens(refreshTokenService.consume(refreshToken));
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private AuthResponse issueTokens(User user) {
        if (!user.isEnabled()) {
            throw new UnauthorizedException("This account has been disabled");
        }
        return AuthResponse.bearer(jwtService.generateAccessToken(user), refreshTokenService.issue(user),
                jwtService.accessTokenTtlSeconds(), UserResponse.from(user));
    }

    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
