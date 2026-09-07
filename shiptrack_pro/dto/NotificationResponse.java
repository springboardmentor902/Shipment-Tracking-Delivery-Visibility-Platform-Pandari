package com.shiptrack.shiptrack_pro.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;

    private Long userId;

    private Long shipmentId;

    private String trackingNumber;

    private String type;

    private String title;

    private String message;

    private String channel;

    private String status;

    private String recipientEmail;

    private String recipientPhone;

    private LocalDateTime sentAt;

    private LocalDateTime deliveredAt;

    private LocalDateTime readAt;

    private Boolean isRead;

    private String failedReason;

    private Integer retryCount;
}


