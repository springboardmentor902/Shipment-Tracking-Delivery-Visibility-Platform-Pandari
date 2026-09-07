package com.shiptrack.shiptrack_pro.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProofOfDeliveryResponse {
    private Long id;
    private String trackingNumber;
    private String signatureUrl;
    private String deliveryPhotoUrl;
    private String deliveredToName;
    private String deliveryNotes;
    private String verificationStatus;
    private LocalDateTime deliveredAt;
    private LocalDateTime verifiedAt;
}
