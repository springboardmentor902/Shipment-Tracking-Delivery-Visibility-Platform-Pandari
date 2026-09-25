package com.shiptrack.shiptrack_pro.service.integration;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushFcmOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FirebaseNotificationService {

    /**
     * Send push notification to a specific device token
     */
    public boolean sendPushNotification(String deviceToken, String title, String body) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(notification)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push notification sent successfully. Message ID: {}", response);
            return true;
        } catch (Exception e) {
            log.error("Error sending push notification to device: {}", deviceToken, e);
            return false;
        }
    }

    /**
     * Send push notification with data payload
     */
    public boolean sendPushNotificationWithData(String deviceToken, String title, String body,
                                                Map<String, String> data) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(notification)
                    .putAllData(data)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push notification with data sent successfully. Message ID: {}", response);
            return true;
        } catch (Exception e) {
            log.error("Error sending push notification with data to device: {}", deviceToken, e);
            return false;
        }
    }

    /**
     * Send push notification for shipment status update
     */
    public boolean sendShipmentStatusNotification(String deviceToken, String trackingNumber,
                                                  String status, String message) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("tracking_number", trackingNumber);
            data.put("status", status);
            data.put("type", "shipment_update");

            String title = "Shipment Update";
            String body = "Your shipment " + trackingNumber + " is now " + status.toLowerCase();

            return sendPushNotificationWithData(deviceToken, title, body, data);
        } catch (Exception e) {
            log.error("Error sending shipment status notification", e);
            return false;
        }
    }

    /**
     * Send push notification for delivery alert
     */
    public boolean sendDeliveryAlert(String deviceToken, String trackingNumber,
                                     String deliveryAddress, String estimatedTime) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("tracking_number", trackingNumber);
            data.put("address", deliveryAddress);
            data.put("estimated_time", estimatedTime);
            data.put("type", "delivery_alert");

            String title = "Out for Delivery";
            String body = "Your package will be delivered to " + deliveryAddress +
                    " by " + estimatedTime;

            return sendPushNotificationWithData(deviceToken, title, body, data);
        } catch (Exception e) {
            log.error("Error sending delivery alert", e);
            return false;
        }
    }

    /**
     * Send push notification for delay warning
     */
    public boolean sendDelayWarning(String deviceToken, String trackingNumber,
                                    String reason, String newEstimate) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("tracking_number", trackingNumber);
            data.put("reason", reason);
            data.put("new_estimate", newEstimate);
            data.put("type", "delay_alert");

            String title = "Delivery Delayed";
            String body = "Your shipment " + trackingNumber + " has been delayed. " +
                    "New estimated delivery: " + newEstimate;

            return sendPushNotificationWithData(deviceToken, title, body, data);
        } catch (Exception e) {
            log.error("Error sending delay warning", e);
            return false;
        }
    }

    /**
     * Send multicast push notification to multiple devices
     */
    public Map<String, Boolean> sendMulticastNotification(java.util.List<String> deviceTokens,
                                                          String title, String body) {
        Map<String, Boolean> results = new HashMap<>();

        for (String token : deviceTokens) {
            results.put(token, sendPushNotification(token, title, body));
        }

        return results;
    }

    /**
     * Send promotional/marketing notification
     */
    public boolean sendPromotionalNotification(String deviceToken, String title,
                                               String body, String actionUrl) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("action_url", actionUrl);
            data.put("type", "promotional");

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(notification)
                    .putAllData(data)
                    .setWebpushConfig(WebpushConfig.builder()
                            .setFcmOptions(WebpushFcmOptions.builder()
                                    .setLink(actionUrl)
                                    .build())
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Promotional notification sent. Message ID: {}", response);
            return true;
        } catch (Exception e) {
            log.error("Error sending promotional notification", e);
            return false;
        }
    }
}
