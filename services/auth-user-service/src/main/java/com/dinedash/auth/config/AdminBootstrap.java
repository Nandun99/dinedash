package com.dinedash.auth.config;

import com.dinedash.auth.domain.User;
import com.dinedash.auth.repository.UserRepository;
import com.dinedash.common.security.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Creates the first admin account from environment variables, so no default
 * credentials ever live in the codebase or migrations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${dinedash.admin.email:}")
    private String adminEmail;

    @Value("${dinedash.admin.password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD not set, skipping admin bootstrap");
            return;
        }
        String email = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        userRepository.save(new User(email, passwordEncoder.encode(adminPassword), "Platform Admin", null, Role.ADMIN));
        log.info("Created bootstrap admin account {}", email);
    }
}
