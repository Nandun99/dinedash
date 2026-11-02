package com.dinedash.delivery.tracking;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Mints a Firebase custom token that only grants read access to one order's tracking node
 * (see infra/firebase/database.rules.json), so customers cannot read other people's deliveries.
 */
@Service
@ConditionalOnProperty(name = "dinedash.firebase.enabled", havingValue = "true")
public class TrackingTokenService {

    public String tokenFor(Long userId, Long orderId) {
        try {
            return FirebaseAuth.getInstance(FirebaseApp.getInstance())
                    .createCustomToken("user-" + userId, Map.of("orderId", String.valueOf(orderId)));
        } catch (FirebaseAuthException e) {
            throw new IllegalStateException("Could not create tracking token", e);
        }
    }
}
