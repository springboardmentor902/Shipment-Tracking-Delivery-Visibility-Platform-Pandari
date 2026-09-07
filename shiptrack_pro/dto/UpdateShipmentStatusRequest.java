package com.shiptrack.shiptrack_pro.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShipmentStatusRequest {

    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;

    @NotBlank(message = "Status is required")
    private String status;

    @NotBlank(message = "Location is required")
    private String location;

    private Double latitude;
    private Double longitude;
    private String notes;
}
