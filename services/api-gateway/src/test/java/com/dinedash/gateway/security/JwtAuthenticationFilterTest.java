package com.dinedash.gateway.security;

import com.dinedash.common.security.AuthHeaders;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "ZGluZWRhc2gtdGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2g=";

    private final GatewaySecurityProperties properties = new GatewaySecurityProperties(
            new GatewaySecurityProperties.Jwt(SECRET, "dinedash-auth"),
            new GatewaySecurityProperties.Security(List.of("/api/auth/**", "GET:/api/restaurants/**")));
    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(properties, new PublicPathMatcher(properties));

    private final AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
    private final GatewayFilterChain chain = exchange -> {
        forwarded.set(exchange);
        return Mono.empty();
    };

    @Test
    void publicPathPassesWithoutToken() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/auth/login"));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertThat(forwarded.get()).isNotNull();
    }

    @Test
    void protectedPathWithoutTokenIsRejected() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/orders"));

        StepVerifier.create(filter.filter(exchange, chain))
                .expectErrorMatches(ex -> ex instanceof ResponseStatusException rse
                        && rse.getStatusCode() == HttpStatus.UNAUTHORIZED)
                .verify();
    }

    @Test
    void validTokenForwardsIdentityHeaders() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("CUSTOMER")));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        HttpHeaders headers = forwarded.get().getRequest().getHeaders();
        assertThat(headers.getFirst(AuthHeaders.USER_ID)).isEqualTo("42");
        assertThat(headers.getFirst(AuthHeaders.USER_ROLE)).isEqualTo("CUSTOMER");
    }

    @Test
    void spoofedIdentityHeadersAreStripped() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/auth/login")
                .header(AuthHeaders.USER_ID, "1")
                .header(AuthHeaders.USER_ROLE, "ADMIN"));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        HttpHeaders headers = forwarded.get().getRequest().getHeaders();
        assertThat(headers.getFirst(AuthHeaders.USER_ID)).isNull();
        assertThat(headers.getFirst(AuthHeaders.USER_ROLE)).isNull();
    }

    @Test
    void nonAdminCannotReachAdminRoutes() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/admin/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("CUSTOMER")));

        StepVerifier.create(filter.filter(exchange, chain))
                .expectErrorMatches(ex -> ex instanceof ResponseStatusException rse
                        && rse.getStatusCode() == HttpStatus.FORBIDDEN)
                .verify();
    }

    private static String token(String role) {
        return Jwts.builder()
                .issuer("dinedash-auth")
                .subject("42")
                .claim("email", "nimal@example.com")
                .claim("role", role)
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();
    }
}
