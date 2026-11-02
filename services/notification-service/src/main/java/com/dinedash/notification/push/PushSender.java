package com.dinedash.notification.push;

import java.util.List;

public interface PushSender {

    /**
     * @return tokens FCM reported as no longer valid, so they can be removed
     */
    List<String> send(List<String> tokens, String title, String body, String link);
}
