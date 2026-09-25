package com.shiptrack.shiptrack_pro.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class CustomerAnalyticsResponse {
    private Long customerId;
    private String customerName;
    private int activeShipmentCount;
    private int totalShipmentCount;
    private Map<String, Long> shipmentStatusBreakdown;
    private List<ShipmentHistoryItem> shipmentHistory;
    private TrackingInsights trackingInsights;

    @Data
    @Builder
    public static class ShipmentHistoryItem {
        private Long shipmentId;
        private String trackingNumber;
        private String status;
        private String priority;
        private String estimatedDeliveryDate;
        private String actualDeliveryDate;
        private String createdAt;
    }

    @Data
    @Builder
    public static class TrackingInsights {
        private long deliveredCount;
        private long inTransitCount;
        private long delayedCount;
        private long cancelledCount;
        private long routesTracked;
        private long activeRoutes;
    }
}
