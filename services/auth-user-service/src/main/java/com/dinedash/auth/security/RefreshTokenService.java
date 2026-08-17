package com.dinedash.auth.security;

import com.dinedash.auth.domain.RefreshToken;
import com.dinedash.auth.domain.User;
import com.dinedash.auth.exception.UnauthorizedException;
import com.dinedash.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;
    private final JwtProperties properties;

    /** Creates a new refresh token and returns the raw value (only ever sent to the client). */
    @Transactional
    public String issue(User user) {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repository.save(new RefreshToken(user, hash(raw), Instant.now().plus(properties.refreshTokenTtl())));
        return raw;
    }

    /**
     * Validates and revokes the presented token (rotation). A reused token means it may have
     * been stolen, so every session of that user is revoked.
     */
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public User consume(String rawToken) {
        RefreshToken token = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (token.isRevoked()) {
            log.warn("Refresh token reuse detected for user id={}", token.getUser().getId());
            repository.revokeAllForUser(token.getUser().getId());
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        if (!token.isActive(Instant.now())) {
            throw new UnauthorizedException("Refresh token has expired");
        }
        token.setRevoked(true);
        return token.getUser();
    }

    @Transactional
    public void revoke(String rawToken) {
        repository.findByTokenHash(hash(rawToken)).ifPresent(token -> token.setRevoked(true));
    }

    @Transactional
    public void revokeAll(Long userId) {
        repository.revokeAllForUser(userId);
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpired() {
        int removed = repository.deleteExpiredBefore(Instant.now().minus(1, ChronoUnit.DAYS));
        if (removed > 0) {
            log.info("Purged {} expired refresh tokens", removed);
        }
    }

    static String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
