
package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.entity.Notification;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.User;

import java.util.List;

public interface NotificationService {

    /**
     * Create and send a notification for a user and shipment.
     *
     * @param type     Notification type
     * @param user     Notification recipient
     * @param shipment Shipment associated with notification
     * @return saved notification
     */
    Notification send(String type, User user, Shipment shipment);

    /**
     * Create and send a notification using custom title/message/channel.
     */
    Notification send(
            String type,
            User user,
            Shipment shipment,
            String title,
            String message,
            String channel
    );

    /**
     * Get all notifications for a user.
     */
    List<Notification> getUserNotifications(Long userId);

    /**
     * Get unread notifications for a user.
     */
    List<Notification> getUnreadNotifications(Long userId);

    /**
     * Mark a notification as read.
     */
    Notification markAsRead(Long notificationId, Long userId);
}