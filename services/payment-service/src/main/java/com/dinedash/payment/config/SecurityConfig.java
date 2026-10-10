package com.dinedash.payment.config;

import com.dinedash.common.security.ServiceSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return ServiceSecurity.statelessDefaults(http)
                .authorizeHttpRequests(auth -> auth
                        // Stripe calls this directly; requests are authenticated by signature instead
                        .requestMatchers("/api/payments/webhooks/**").permitAll()
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .anyRequest().hasRole("CUSTOMER"))
                .build();
    }
}
