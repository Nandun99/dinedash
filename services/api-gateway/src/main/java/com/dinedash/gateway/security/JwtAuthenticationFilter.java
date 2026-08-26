package com.dinedash.gateway.security;

import com.dinedash.common.security.AuthHeaders;
import com.dinedash.common.security.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Validates the bearer token once, at the edge, and forwards the caller identity as
 * trusted headers. Any identity headers sent by the client are stripped first so they
 * cannot be spoofed.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ADMIN_PATH = "/api/admin/";

    private final JwtParser parser;
    private final PublicPathMatcher publicPaths;

    public JwtAuthenticationFilter(GatewaySecurityProperties properties, PublicPathMatcher publicPaths) {
        this.parser = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.jwt().secret())))
                .requireIssuer(properties.jwt().issuer())
                .build();
        this.publicPaths = publicPaths;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(h -> {
                    h.remove(AuthHeaders.USER_ID);
                    h.remove(AuthHeaders.USER_EMAIL);
                    h.remove(AuthHeaders.USER_ROLE);
                })
                .build();
        String path = request.getPath().value();
        boolean isPublic = publicPaths.isPublic(request.getMethod(), path);
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            if (isPublic) {
                return chain.filter(exchange.mutate().request(request).build());
            }
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token"));
        }

        Claims claims;
        try {
            claims = parser.parseSignedClaims(header.substring(BEARER_PREFIX.length())).getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid token on {}: {}", path, ex.getMessage());
            if (isPublic) {
                return chain.filter(exchange.mutate().request(request).build());
            }
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token"));
        }

        String role = claims.get("role", String.class);
        if (path.startsWith(ADMIN_PATH) && !Role.ADMIN.name().equals(role)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required"));
        }

        ServerHttpRequest authenticated = request.mutate()
                .header(AuthHeaders.USER_ID, claims.getSubject())
                .header(AuthHeaders.USER_EMAIL, String.valueOf(claims.get("email", String.class)))
                .header(AuthHeaders.USER_ROLE, role)
                .build();
        return chain.filter(exchange.mutate().request(authenticated).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
