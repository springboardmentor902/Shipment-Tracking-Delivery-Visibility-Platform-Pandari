package com.shiptrack.shiptrack_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateResponse {

    private Long routeId;

    private Long shipmentId;

    private Double latitude;

    private Double longitude;

    private LocalDateTime timestamp;
}
