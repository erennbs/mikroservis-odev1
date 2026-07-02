package com.etiya.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// notification-service'in veritabani yoktur: bildirimler bellekte tutulur (NotificationStore).
// payment-created olayini tuketip her odeme icin bir bildirim uretir; CRUD ise bu in-memory
// listenin uzerinde calisir.
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
