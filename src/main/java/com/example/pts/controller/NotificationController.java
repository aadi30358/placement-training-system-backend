package com.example.pts.controller;

import com.example.pts.model.Notification;
import com.example.pts.repository.NotificationRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return notificationRepository.findByUserIdOrderByDateDesc(userId);
    }

    @PostMapping
    public Notification createNotification(@RequestBody Notification notification) {
        if (notification.getDate() == null) {
            notification.setDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        if (notification.getReadStatus() == null) {
            notification.setReadStatus(false);
        }
        return notificationRepository.save(notification);
    }

    @PutMapping("/{id}/read")
    public Notification markAsRead(@PathVariable Long id) {
        return notificationRepository.findById(id).map(notif -> {
            notif.setReadStatus(true);
            return notificationRepository.save(notif);
        }).orElseThrow(() -> new RuntimeException("Notification not found"));
    }
}
