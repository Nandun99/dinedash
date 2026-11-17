package com.dinedash.delivery.tracking;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {

    /**
     * Credentials are read from GOOGLE_APPLICATION_CREDENTIALS (a service account JSON
     * mounted as a secret), never from the repository.
     */
    @Bean
    @ConditionalOnProperty(name = "dinedash.firebase.enabled", havingValue = "true")
    public FirebaseDatabase firebaseDatabase(@Value("${dinedash.firebase.database-url}") String databaseUrl)
            throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials())
                .setDatabaseUrl(databaseUrl)
                .build();
        FirebaseApp app = FirebaseApp.getApps().isEmpty() ? FirebaseApp.initializeApp(options) : FirebaseApp.getInstance();
        log.info("Firebase live tracking enabled ({})", databaseUrl);
        return FirebaseDatabase.getInstance(app);
    }

    @Bean
    @ConditionalOnProperty(name = "dinedash.firebase.enabled", havingValue = "true")
    public LiveTrackingPublisher firebaseTrackingPublisher(FirebaseDatabase database) {
        return new FirebaseTrackingPublisher(database);
    }

    @Bean
    @ConditionalOnMissingBean(LiveTrackingPublisher.class)
    public LiveTrackingPublisher noopTrackingPublisher() {
        log.info("Firebase disabled; customers will poll for driver location");
        return new LiveTrackingPublisher() {
            @Override
            public void publishLocation(Long orderId, double latitude, double longitude) {
            }

            @Override
            public void publishStatus(com.dinedash.delivery.domain.Delivery delivery) {
            }
        };
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
