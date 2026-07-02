package com.etiya.notificationservice.services.concretes;

import com.etiya.notificationservice.model.Notification;
import com.etiya.notificationservice.services.abstracts.NotificationService;
import com.etiya.notificationservice.services.dtos.requests.CreateNotificationRequest;
import com.etiya.notificationservice.services.dtos.requests.UpdateNotificationRequest;
import com.etiya.notificationservice.services.dtos.responses.CreatedNotificationResponse;
import com.etiya.notificationservice.services.dtos.responses.DeletedNotificationResponse;
import com.etiya.notificationservice.services.dtos.responses.GetAllNotificationsResponse;
import com.etiya.notificationservice.services.dtos.responses.GetByIdNotificationResponse;
import com.etiya.notificationservice.services.dtos.responses.UpdatedNotificationResponse;
import com.etiya.notificationservice.services.exceptions.BusinessException;
import com.etiya.notificationservice.store.NotificationStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business layer implementation backed by the in-memory {@link NotificationStore} (no database).
 * Maps between request/response DTOs and the {@link Notification} model.
 */
@Service
public class NotificationManager implements NotificationService {

    private final NotificationStore notificationStore;

    public NotificationManager(NotificationStore notificationStore) {
        this.notificationStore = notificationStore;
    }

    @Override
    public CreatedNotificationResponse add(CreateNotificationRequest request) {
        Notification created = notificationStore.create(
                request.getCustomerId(), request.getOrderId(), request.getMessage());
        return new CreatedNotificationResponse(
                created.getId(),
                created.getCustomerId(),
                created.getOrderId(),
                created.getMessage(),
                created.getCreatedAt());
    }

    @Override
    public UpdatedNotificationResponse update(UpdateNotificationRequest request) {
        Notification updated = notificationStore.update(
                        request.getId(), request.getCustomerId(), request.getOrderId(), request.getMessage())
                .orElseThrow(() -> notFound(request.getId()));
        return new UpdatedNotificationResponse(
                updated.getId(),
                updated.getCustomerId(),
                updated.getOrderId(),
                updated.getMessage(),
                updated.getCreatedAt());
    }

    @Override
    public DeletedNotificationResponse delete(int id) {
        Notification deleted = notificationStore.delete(id)
                .orElseThrow(() -> notFound(id));
        return new DeletedNotificationResponse(deleted.getId(), deleted.getCustomerId());
    }

    @Override
    public List<GetAllNotificationsResponse> getAll() {
        return notificationStore.findAll().stream()
                .map(notification -> new GetAllNotificationsResponse(
                        notification.getId(),
                        notification.getCustomerId(),
                        notification.getOrderId(),
                        notification.getMessage(),
                        notification.getCreatedAt()))
                .toList();
    }

    @Override
    public GetByIdNotificationResponse getById(int id) {
        Notification notification = notificationStore.findById(id)
                .orElseThrow(() -> notFound(id));
        return new GetByIdNotificationResponse(
                notification.getId(),
                notification.getCustomerId(),
                notification.getOrderId(),
                notification.getMessage(),
                notification.getCreatedAt());
    }

    private BusinessException notFound(int id) {
        return new BusinessException("Notification not found with id: " + id);
    }
}
