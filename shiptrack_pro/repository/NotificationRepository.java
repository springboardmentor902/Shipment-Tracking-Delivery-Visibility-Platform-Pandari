package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get all notifications for a user, newest first
    List<Notification> findByUser_IdOrderBySentAtDesc(Long userId);

    // Get unread notifications
    List<Notification> findByUser_IdAndIsReadFalse(Long userId);

    // Get notifications for a shipment by channel
    List<Notification> findByShipment_IdAndChannel(
            Long shipmentId,
            String channel
    );

    // Get notifications by status
    List<Notification> findByStatus(String status);

    // Prevent duplicate notifications within a recent time period
    boolean existsByUser_IdAndShipment_IdAndNotificationTypeAndSentAtAfter(
            Long userId,
            Long shipmentId,
            String notificationType,
            LocalDateTime after
    );
}