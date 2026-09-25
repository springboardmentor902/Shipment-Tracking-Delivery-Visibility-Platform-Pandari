package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.entity.Notification;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.NotificationRepository;
import com.shiptrack.shiptrack_pro.service.NotificationService;
import com.shiptrack.shiptrack_pro.service.integration.TwilioSmsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender javaMailSender;
    private final TwilioSmsService twilioSmsService;

    private static final int DUPLICATE_WINDOW_MINUTES = 30;

    // ============================================================
    // SEND DEFAULT NOTIFICATION
    // ============================================================

    @Override
    public Notification send(
            String type,
            User user,
            Shipment shipment
    ) {

        String title;
        String message;
        String channel = "EMAIL";

        // Shipment update
        if ("SHIPMENT_UPDATE".equalsIgnoreCase(type)) {

            title = "Shipment Status Updated";

            message = String.format(
                    "Your shipment %s status has been updated to %s.",
                    shipment.getTrackingNumber(),
                    shipment.getStatus()
            );

        }

        // Delay warning
        else if ("DELAY_WARNING".equalsIgnoreCase(type)) {

            title = "Shipment Delay Warning";

            message = String.format(
                    "Your shipment %s may be delayed. Please check the latest tracking information.",
                    shipment.getTrackingNumber()
            );

            channel = "EMAIL";

        }

        // Other notification
        else {

            title = "Shipment Notification";

            message = String.format(
                    "There is an update regarding your shipment %s.",
                    shipment.getTrackingNumber()
            );
        }

        return send(
                type,
                user,
                shipment,
                title,
                message,
                channel
        );
    }

    // ============================================================
    // MAIN SEND METHOD
    // ============================================================

    @Override
    public Notification send(
            String type,
            User user,
            Shipment shipment,
            String title,
            String message,
            String channel
    ) {

        // Validation
        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        if (shipment == null) {
            throw new IllegalArgumentException(
                    "Shipment cannot be null"
            );
        }

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                    "Notification type cannot be empty"
            );
        }

        // ========================================================
        // DUPLICATE CHECK
        // ========================================================

        LocalDateTime duplicateCheckTime =
                LocalDateTime.now()
                        .minusMinutes(DUPLICATE_WINDOW_MINUTES);

        boolean alreadySent =
                notificationRepository
                        .existsByUser_IdAndShipment_IdAndNotificationTypeAndSentAtAfter(
                                user.getId(),
                                shipment.getId(),
                                type,
                                duplicateCheckTime
                        );

        if (alreadySent) {

            log.info(
                    "Duplicate notification prevented. Type: {}, User: {}, Shipment: {}",
                    type,
                    user.getId(),
                    shipment.getId()
            );

            return notificationRepository
                    .findByUser_IdOrderBySentAtDesc(user.getId())
                    .stream()
                    .filter(notification ->
                            notification.getShipment() != null
                                    && notification.getShipment()
                                    .getId()
                                    .equals(shipment.getId())
                                    && notification.getNotificationType()
                                    .equalsIgnoreCase(type)
                    )
                    .findFirst()
                    .orElse(null);
        }

        // ========================================================
        // CREATE NOTIFICATION
        // ========================================================

        Notification notification = Notification.builder()
                .user(user)
                .shipment(shipment)
                .notificationType(type)
                .title(title)
                .message(message)
                .channel(channel)
                .status("PENDING")
                .recipientEmail(user.getEmail())
                .recipientPhone(user.getPhone())
                .isRead(false)
                .retryCount(0)

                // sent_at is NOT NULL in the database.
                // Therefore, it must be set before save().
                .sentAt(LocalDateTime.now())

                .build();

        // ========================================================
        // FIRST DATABASE SAVE
        // ========================================================

        notification =
                notificationRepository.save(notification);

        // No initializer is needed because every branch below
        // assigns a value to delivered.
        boolean delivered;

        // ========================================================
        // SEND NOTIFICATION
        // ========================================================

        try {

            // ----------------------------------------------------
            // EMAIL
            // ----------------------------------------------------

            if ("EMAIL".equalsIgnoreCase(channel)) {

                delivered =
                        sendEmail(
                                user.getEmail(),
                                title,
                                message
                        );
            }

            // ----------------------------------------------------
            // SMS
            // ----------------------------------------------------

            else if ("SMS".equalsIgnoreCase(channel)) {

                delivered =
                        sendSms(
                                user.getPhone(),
                                shipment,
                                type,
                                message
                        );
            }

            // ----------------------------------------------------
            // IN-APP
            // ----------------------------------------------------

            else if ("IN_APP".equalsIgnoreCase(channel)) {

                log.info(
                        "In-app notification created successfully for user {}",
                        user.getId()
                );

                delivered = true;
            }

            // ----------------------------------------------------
            // PUSH
            // ----------------------------------------------------

            else if ("PUSH".equalsIgnoreCase(channel)) {

                log.warn(
                        "PUSH notification requested but push service is not configured."
                );

                delivered = false;
            }

            // ----------------------------------------------------
            // UNKNOWN CHANNEL
            // ----------------------------------------------------

            else {

                log.warn(
                        "Unknown notification channel: {}",
                        channel
                );

                delivered = false;
            }

            // ====================================================
            // UPDATE NOTIFICATION STATUS
            // ====================================================

            if (delivered) {

                notification.setStatus("SENT");

                notification.setDeliveredAt(
                        LocalDateTime.now()
                );

                log.info(
                        "Notification sent successfully. User: {}, Shipment: {}, Type: {}, Channel: {}",
                        user.getId(),
                        shipment.getId(),
                        type,
                        channel
                );

            } else {

                notification.setStatus("FAILED");

                notification.setFailedReason(
                        "Notification delivery failed"
                );

                notification.setRetryCount(
                        notification.getRetryCount() == null
                                ? 1
                                : notification.getRetryCount() + 1
                );

                log.warn(
                        "Notification delivery failed. User: {}, Shipment: {}, Type: {}, Channel: {}",
                        user.getId(),
                        shipment.getId(),
                        type,
                        channel
                );
            }

        }

        // ========================================================
        // EXCEPTION HANDLING
        // ========================================================

        catch (Exception e) {

            notification.setStatus("FAILED");

            notification.setFailedReason(
                    e.getMessage()
            );

            notification.setRetryCount(
                    notification.getRetryCount() == null
                            ? 1
                            : notification.getRetryCount() + 1
            );

            log.error(
                    "Exception while sending notification",
                    e
            );
        }

        // ========================================================
        // FINAL DATABASE SAVE
        // ========================================================

        return notificationRepository.save(notification);
    }

    // ============================================================
    // SEND EMAIL
    // ============================================================

    private boolean sendEmail(
            String email,
            String subject,
            String message
    ) {

        if (email == null || email.isBlank()) {

            log.warn(
                    "Cannot send email because recipient email is empty."
            );

            return false;
        }

        try {

            SimpleMailMessage mailMessage =
                    new SimpleMailMessage();

            mailMessage.setTo(email);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            javaMailSender.send(mailMessage);

            log.info(
                    "Email sent successfully to {}",
                    email
            );

            return true;

        } catch (Exception e) {

            log.error(
                    "Failed to send email to {}: {}",
                    email,
                    e.getMessage(),
                    e
            );

            return false;
        }
    }

    // ============================================================
    // SEND SMS
    // ============================================================

    private boolean sendSms(
            String phone,
            Shipment shipment,
            String type,
            String message
    ) {

        if (phone == null || phone.isBlank()) {

            log.warn(
                    "Cannot send SMS because phone number is empty."
            );

            return false;
        }

        try {

            // Delay warning SMS
            if ("DELAY_WARNING".equalsIgnoreCase(type)) {

                return twilioSmsService
                        .sendDelayNotificationSms(
                                phone,
                                shipment.getTrackingNumber(),
                                "possible delivery delay",
                                shipment.getEstimatedDeliveryDate() != null
                                        ? shipment
                                        .getEstimatedDeliveryDate()
                                        .toString()
                                        : "currently unavailable"
                        );
            }

            // Normal SMS
            return twilioSmsService.sendSms(
                    phone,
                    message
            );

        } catch (Exception e) {

            log.error(
                    "Failed to send SMS to {}: {}",
                    phone,
                    e.getMessage(),
                    e
            );

            return false;
        }
    }

    // ============================================================
    // GET USER NOTIFICATIONS
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(
            Long userId
    ) {

        return notificationRepository
                .findByUser_IdOrderBySentAtDesc(userId);
    }

    // ============================================================
    // GET UNREAD NOTIFICATIONS
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(
            Long userId
    ) {

        return notificationRepository
                .findByUser_IdAndIsReadFalse(userId);
    }

    // ============================================================
    // MARK AS READ
    // ============================================================

    @Override
    public Notification markAsRead(
            Long notificationId,
            Long userId
    ) {

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Notification not found"
                                )
                        );

        // Check notification ownership
        if (!notification
                .getUser()
                .getId()
                .equals(userId)) {

            throw new RuntimeException(
                    "You are not authorized to update this notification"
            );
        }

        // Mark as read
        notification.setIsRead(true);

        notification.setReadAt(
                LocalDateTime.now()
        );

        notification.setStatus("READ");

        return notificationRepository.save(
                notification
        );
    }
}
