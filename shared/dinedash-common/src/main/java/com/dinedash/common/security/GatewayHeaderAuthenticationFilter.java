package com.dinedash.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Builds the security context from the identity headers added by the gateway.
 * Services are only reachable inside the cluster network, and the gateway strips
 * these headers from client requests, so they can be trusted here.
 */
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String id = request.getHeader(AuthHeaders.USER_ID);
        String role = request.getHeader(AuthHeaders.USER_ROLE);
        if (id != null && role != null) {
            try {
                CurrentUser user = new CurrentUser(Long.valueOf(id), request.getHeader(AuthHeaders.USER_EMAIL),
                        Role.valueOf(role));
                var authentication = new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
