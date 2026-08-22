package com.dinedash.auth.security;

import com.dinedash.auth.domain.RefreshToken;
import com.dinedash.auth.domain.User;
import com.dinedash.auth.exception.UnauthorizedException;
import com.dinedash.auth.repository.RefreshTokenRepository;
import com.dinedash.common.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;

    private RefreshTokenService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(repository, JwtServiceTest.props("x", Duration.ofMinutes(15)));
        user = new User("kamal@example.com", "hash", "Kamal Perera", null, Role.CUSTOMER);
        user.setId(7L);
    }

    @Test
    void issueStoresOnlyHashOfToken() {
        when(repository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        String raw = service.issue(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).isEqualTo(RefreshTokenService.hash(raw)).isNotEqualTo(raw);
        assertThat(captor.getValue().getExpiresAt()).isAfter(Instant.now().plus(Duration.ofDays(6)));
    }

    @Test
    void consumeRevokesTokenAndReturnsUser() {
        RefreshToken token = new RefreshToken(user, RefreshTokenService.hash("abc"), Instant.now().plusSeconds(60));
        when(repository.findByTokenHash(RefreshTokenService.hash("abc"))).thenReturn(Optional.of(token));

        assertThat(service.consume("abc")).isSameAs(user);
        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    void reusingRevokedTokenRevokesAllSessions() {
        RefreshToken token = new RefreshToken(user, RefreshTokenService.hash("abc"), Instant.now().plusSeconds(60));
        token.setRevoked(true);
        when(repository.findByTokenHash(RefreshTokenService.hash("abc"))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.consume("abc")).isInstanceOf(UnauthorizedException.class);
        verify(repository).revokeAllForUser(7L);
    }

    @Test
    void expiredTokenIsRejected() {
        RefreshToken token = new RefreshToken(user, RefreshTokenService.hash("abc"), Instant.now().minusSeconds(1));
        when(repository.findByTokenHash(RefreshTokenService.hash("abc"))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.consume("abc"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired");
    }
}
