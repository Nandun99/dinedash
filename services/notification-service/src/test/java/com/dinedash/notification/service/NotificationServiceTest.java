package com.dinedash.notification.service;

import com.dinedash.notification.domain.DeviceToken;
import com.dinedash.notification.domain.Notification;
import com.dinedash.notification.push.PushSender;
import com.dinedash.notification.repository.DeviceTokenRepository;
import com.dinedash.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;
    @Mock
    private DeviceTokenRepository deviceTokens;
    @Mock
    private PushSender pushSender;

    @InjectMocks
    private NotificationService service;

    @Test
    void storesInInboxPushesAndPrunesDeadTokens() {
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deviceTokens.findByUserId(10L)).thenReturn(List.of(new DeviceToken("good", 10L), new DeviceToken("dead", 10L)));
        when(pushSender.send(List.of("good", "dead"), "On the way", "body", "/orders/1")).thenReturn(List.of("dead"));

        service.notify(10L, "On the way", "body", 1L, "/orders/1");

        verify(repository).save(any(Notification.class));
        verify(deviceTokens).deleteAllById(List.of("dead"));
    }
}
