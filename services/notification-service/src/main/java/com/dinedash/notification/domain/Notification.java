package com.dinedash.notification.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** In-app inbox entry. Push is best effort; the inbox is always there. */
@Document(collection = "notifications")
@CompoundIndex(name = "user_created", def = "{'userId': 1, 'createdAt': -1}")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    private String id;

    private Long userId;

    private String title;

    private String body;

    private Long orderId;

    private String link;

    private boolean read;

    private Instant createdAt;

    public Notification(Long userId, String title, String body, Long orderId, String link) {
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.orderId = orderId;
        this.link = link;
        this.createdAt = Instant.now();
    }
}
