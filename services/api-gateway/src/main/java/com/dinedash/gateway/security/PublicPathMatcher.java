package com.dinedash.gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * Decides whether a request may pass without a bearer token.
 * Entries look like {@code /api/auth/**} or {@code GET:/api/restaurants/**}.
 */
@Component
public class PublicPathMatcher {

    private final AntPathMatcher matcher = new AntPathMatcher();
    private final List<Rule> rules;

    public PublicPathMatcher(GatewaySecurityProperties properties) {
        this.rules = properties.security().publicPaths().stream().map(Rule::parse).toList();
    }

    public boolean isPublic(HttpMethod method, String path) {
        return rules.stream().anyMatch(rule ->
                (rule.method() == null || rule.method().equals(method)) && matcher.match(rule.pattern(), path));
    }

    private record Rule(HttpMethod method, String pattern) {
        static Rule parse(String value) {
            int idx = value.indexOf(':');
            if (idx > 0 && !value.startsWith("/")) {
                return new Rule(HttpMethod.valueOf(value.substring(0, idx).trim()), value.substring(idx + 1).trim());
            }
            return new Rule(null, value.trim());
        }
    }
}
