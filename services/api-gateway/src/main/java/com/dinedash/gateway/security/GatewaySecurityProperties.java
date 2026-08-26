package com.dinedash.gateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "dinedash")
public record GatewaySecurityProperties(Jwt jwt, Security security) {

    public record Jwt(String secret, String issuer) {
    }

    public record Security(List<String> publicPaths) {
        public Security {
            publicPaths = publicPaths == null ? List.of() : List.copyOf(publicPaths);
        }
    }
}
