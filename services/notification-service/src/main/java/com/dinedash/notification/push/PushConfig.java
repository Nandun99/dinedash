package com.dinedash.notification.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Configuration
public class PushConfig {

    @Bean
    @ConditionalOnProperty(name = "dinedash.firebase.enabled", havingValue = "true")
    public PushSender fcmPushSender(@Value("${dinedash.app-base-url:http://localhost:5173}") String appBaseUrl)
            throws IOException {
        FirebaseApp app = FirebaseApp.getApps().isEmpty()
                ? FirebaseApp.initializeApp(FirebaseOptions.builder()
                        .setCredentials(credentials())
                        .build())
                : FirebaseApp.getInstance();
        log.info("FCM web push enabled");
        return new FcmPushSender(FirebaseMessaging.getInstance(app), appBaseUrl);
    }

    @Bean
    @ConditionalOnMissingBean(PushSender.class)
    public PushSender noopPushSender() {
        log.info("Firebase disabled; notifications are in-app only");
        return (tokens, title, body, link) -> List.of();
    }

    /**
     * ECS injects the service account JSON from SSM as FIREBASE_CREDENTIALS_JSON;
     * locally and in Kubernetes GOOGLE_APPLICATION_CREDENTIALS points at a mounted file.
     */
    static GoogleCredentials credentials() throws IOException {
        String json = System.getenv("FIREBASE_CREDENTIALS_JSON");
        if (json != null && !json.isBlank()) {
            return GoogleCredentials.fromStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        }
        return GoogleCredentials.getApplicationDefault();
    }
}
