package com.dinedash.notification.push;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushFcmOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FcmPushSender implements PushSender {

    private final FirebaseMessaging messaging;
    private final String appBaseUrl;

    public FcmPushSender(FirebaseMessaging messaging, String appBaseUrl) {
        this.messaging = messaging;
        this.appBaseUrl = appBaseUrl;
    }

    @Override
    public List<String> send(List<String> tokens, String title, String body, String link) {
        if (tokens.isEmpty()) {
            return List.of();
        }
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .setWebpushConfig(WebpushConfig.builder()
                        .setFcmOptions(WebpushFcmOptions.withLink(appBaseUrl + link))
                        .build())
                .build();
        try {
            BatchResponse response = messaging.sendEachForMulticast(message);
            List<String> invalid = new ArrayList<>();
            for (int i = 0; i < response.getResponses().size(); i++) {
                var r = response.getResponses().get(i);
                if (!r.isSuccessful() && r.getException() != null
                        && r.getException().getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                    invalid.add(tokens.get(i));
                }
            }
            return invalid;
        } catch (FirebaseMessagingException e) {
            log.warn("FCM send failed: {}", e.getMessage());
            return List.of();
        }
    }
}
