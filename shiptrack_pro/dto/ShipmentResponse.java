


package com.shiptrack.shiptrack_pro.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {
    private Long id;
    private String trackingNumber;
    private String senderName;
    private String senderPhone;
    private String senderEmail;
    private String senderAddress;

    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    private String receiverAddress;

    private String status;
    private String priority;
    private String packageDescription;
    private BigDecimal packageWeightKg;

    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
