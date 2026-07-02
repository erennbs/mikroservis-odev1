package com.etiya.notificationservice.services.abstracts;

import com.etiya.notificationservice.services.dtos.requests.CreateNotificationRequest;
import com.etiya.notificationservice.services.dtos.requests.UpdateNotificationRequest;
import com.etiya.notificationservice.services.dtos.responses.CreatedNotificationResponse;
import com.etiya.notificationservice.services.dtos.responses.DeletedNotificationResponse;
import com.etiya.notificationservice.services.dtos.responses.GetAllNotificationsResponse;
import com.etiya.notificationservice.services.dtos.responses.GetByIdNotificationResponse;
import com.etiya.notificationservice.services.dtos.responses.UpdatedNotificationResponse;

import java.util.List;

/**
 * Business layer contract. Controllers depend on this abstraction, never on the concrete manager.
 * All operations run against the in-memory {@link com.etiya.notificationservice.store.NotificationStore}.
 */
public interface NotificationService {

    CreatedNotificationResponse add(CreateNotificationRequest request);

    UpdatedNotificationResponse update(UpdateNotificationRequest request);

    DeletedNotificationResponse delete(int id);

    List<GetAllNotificationsResponse> getAll();

    GetByIdNotificationResponse getById(int id);
}
