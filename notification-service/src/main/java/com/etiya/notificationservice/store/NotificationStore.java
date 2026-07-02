package com.etiya.notificationservice.store;

import com.etiya.notificationservice.model.Notification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe, in-memory store for notifications. Replaces the database that the other services use;
 * everything here is lost on restart, which is acceptable for this service by design.
 */
@Component
public class NotificationStore {

    private final ConcurrentMap<Integer, Notification> notifications = new ConcurrentHashMap<>();
    private final AtomicInteger idSequence = new AtomicInteger(0);

    /** Creates a notification with a generated id and stores it. */
    public Notification create(int customerId, int orderId, String message) {
        int id = idSequence.incrementAndGet();
        Notification notification = new Notification(id, customerId, orderId, message, Instant.now());
        notifications.put(id, notification);
        return notification;
    }

    public List<Notification> findAll() {
        return new ArrayList<>(notifications.values());
    }

    public Optional<Notification> findById(int id) {
        return Optional.ofNullable(notifications.get(id));
    }

    public boolean exists(int id) {
        return notifications.containsKey(id);
    }

    /** Replaces mutable fields of an existing notification; returns the updated record. */
    public Optional<Notification> update(int id, int customerId, int orderId, String message) {
        return Optional.ofNullable(notifications.computeIfPresent(id, (key, existing) -> {
            existing.setCustomerId(customerId);
            existing.setOrderId(orderId);
            existing.setMessage(message);
            return existing;
        }));
    }

    public Optional<Notification> delete(int id) {
        return Optional.ofNullable(notifications.remove(id));
    }
}
