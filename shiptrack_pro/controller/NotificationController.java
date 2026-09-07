package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.NotificationRequest;
import com.shiptrack.shiptrack_pro.entity.Notification;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;

    // ============================================================
    // GET ALL NOTIFICATIONS FOR LOGGED-IN USER
    // GET /api/notifications
    // ============================================================

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        List<Notification> notifications =
                notificationService.getUserNotifications(user.getId());

        return ResponseEntity.ok(notifications);
    }

    // ============================================================
    // GET UNREAD NOTIFICATIONS
    // GET /api/notifications/unread
    // ============================================================

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        List<Notification> notifications =
                notificationService.getUnreadNotifications(user.getId());

        return ResponseEntity.ok(notifications);
    }

    // ============================================================
    // CREATE NOTIFICATION
    // POST /api/notifications
    // ============================================================

    @PostMapping
    public ResponseEntity<Notification> createNotification(
            @Valid @RequestBody NotificationRequest request
    ) {

        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: "
                                        + request.getUserId()
                        )
                );

        Shipment shipment = shipmentRepository
                .findById(request.getShipmentId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Shipment not found with ID: "
                                        + request.getShipmentId()
                        )
                );

        Notification notification =
                notificationService.send(
                        request.getType(),
                        user,
                        shipment,
                        request.getTitle(),
                        request.getMessage(),
                        request.getChannel() != null
                                ? request.getChannel()
                                : "EMAIL"
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notification);
    }

    // ============================================================
    // MARK NOTIFICATION AS READ
    // PATCH /api/notifications/{id}/read
    // ============================================================

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        Notification notification =
                notificationService.markAsRead(
                        id,
                        user.getId()
                );

        return ResponseEntity.ok(notification);
    }

    // ============================================================
    // GET AUTHENTICATED USER
    // ============================================================

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );
    }
}
