package com.dinedash.notification.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** A browser registered for Firebase Cloud Messaging web push. The FCM token is the id. */
@Document(collection = "device_tokens")
@Getter
@Setter
@NoArgsConstructor
public class DeviceToken {

    @Id
    private String token;

    @Indexed
    private Long userId;

    private Instant updatedAt;

    public DeviceToken(String token, Long userId) {
        this.token = token;
        this.userId = userId;
        this.updatedAt = Instant.now();
    }
}
