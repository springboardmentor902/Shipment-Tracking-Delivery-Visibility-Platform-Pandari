package com.shiptrack.shiptrack_pro.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    private Long id;
    private String driverName;
    private String routeStatus;
    private String origin;
    private String destination;
    private Double distanceKm;
    private Integer estimatedTimeMinutes;
    private Integer actualTimeMinutes;
    private String trafficCondition;
    private Integer totalShipments;
    private Integer deliveredCount;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
