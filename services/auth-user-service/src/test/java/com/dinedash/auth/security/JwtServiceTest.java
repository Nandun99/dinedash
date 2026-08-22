package com.dinedash.auth.security;

import com.dinedash.auth.domain.User;
import com.dinedash.common.security.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "ZGluZWRhc2gtdGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2g=";
    private static final String OTHER_SECRET = "YW5vdGhlci1zZWNyZXQta2V5LXRoYXQtaXMtYWxzby1sb25nLWVub3VnaA==";

    private final JwtService jwtService = new JwtService(props(SECRET, Duration.ofMinutes(15)));

    @Test
    void generatedTokenCarriesUserClaims() {
        String token = jwtService.generateAccessToken(user());

        Claims claims = jwtService.parse(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get(JwtService.CLAIM_EMAIL, String.class)).isEqualTo("nimal@example.com");
        assertThat(claims.get(JwtService.CLAIM_ROLE, String.class)).isEqualTo("CUSTOMER");
        assertThat(claims.getIssuer()).isEqualTo("dinedash-auth");
    }

    @Test
    void rejectsTokenSignedWithDifferentKey() {
        String token = new JwtService(props(OTHER_SECRET, Duration.ofMinutes(15))).generateAccessToken(user());

        assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(SignatureException.class);
    }

    @Test
    void rejectsExpiredToken() {
        String token = new JwtService(props(SECRET, Duration.ofSeconds(-5))).generateAccessToken(user());

        assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(ExpiredJwtException.class);
    }

    static JwtProperties props(String secret, Duration ttl) {
        return new JwtProperties(secret, "dinedash-auth", ttl, Duration.ofDays(7));
    }

    private static User user() {
        User user = new User("nimal@example.com", "hash", "Nimal Silva", null, Role.CUSTOMER);
        user.setId(42L);
        return user;
    }
}
