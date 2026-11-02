package com.dinedash.notification.service;

import com.dinedash.common.error.NotFoundException;
import com.dinedash.notification.domain.Notification;
import com.dinedash.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    public Notification notify(Long userId, String title, String body, Long orderId, String link) {
        Notification saved = repository.save(new Notification(userId, title, body, orderId, link));
        log.debug("Notified user {}: {}", userId, title);
        return saved;
    }

    public List<Notification> latest(Long userId) {
        return repository.findTop30ByUserIdOrderByCreatedAtDesc(userId);
    }

    public long unreadCount(Long userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }

    public void markRead(Long userId, String id) {
        Notification n = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        n.setRead(true);
        repository.save(n);
    }

    public void markAllRead(Long userId) {
        List<Notification> unread = repository.findByUserIdAndReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        repository.saveAll(unread);
    }
}
