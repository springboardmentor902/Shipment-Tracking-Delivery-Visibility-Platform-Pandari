package com.shiptrack.shiptrack_pro.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProofOfDeliveryRequest {

    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;

    @NotBlank(message = "Delivered to name is required")
    private String deliveredToName;

    private String recipientPhone;
    private String deliveryNotes;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String deliveryAddress;
}
