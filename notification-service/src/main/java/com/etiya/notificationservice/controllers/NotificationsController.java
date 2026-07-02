package com.etiya.notificationservice.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.etiya.notificationservice.services.abstracts.NotificationService;
import com.etiya.notificationservice.services.dtos.requests.CreateNotificationRequest;
import com.etiya.notificationservice.services.dtos.requests.UpdateNotificationRequest;
import com.etiya.notificationservice.services.dtos.responses.CreatedNotificationResponse;
import com.etiya.notificationservice.services.dtos.responses.DeletedNotificationResponse;
import com.etiya.notificationservice.services.dtos.responses.GetAllNotificationsResponse;
import com.etiya.notificationservice.services.dtos.responses.GetByIdNotificationResponse;
import com.etiya.notificationservice.services.dtos.responses.UpdatedNotificationResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {

    private final NotificationService notificationService;

    public NotificationsController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<GetAllNotificationsResponse> getAll() {
        return notificationService.getAll();
    }

    @GetMapping("/{id}")
    public GetByIdNotificationResponse getById(@PathVariable int id) {
        return notificationService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedNotificationResponse add(@Valid @RequestBody CreateNotificationRequest request) {
        return notificationService.add(request);
    }

    @PutMapping("/{id}")
    public UpdatedNotificationResponse update(@PathVariable int id, @Valid @RequestBody UpdateNotificationRequest request) {
        request.setId(id);
        return notificationService.update(request);
    }

    @DeleteMapping("/{id}")
    public DeletedNotificationResponse delete(@PathVariable int id) {
        return notificationService.delete(id);
    }
}
