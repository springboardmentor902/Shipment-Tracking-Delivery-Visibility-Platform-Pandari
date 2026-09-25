package com.shiptrack.shiptrack_pro.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class BusinessAnalyticsResponse {
    private Long businessId;
    private String businessName;
    private ShipmentAnalytics shipmentAnalytics;
    private DeliveryPerformance deliveryPerformance;
    private DelayAnalysis delayAnalysis;
    private CustomerActivity customerActivity;
    private LogisticsOverview logisticsOverview;

    @Data @Builder
    public static class ShipmentAnalytics {
        private long totalShipments;
        private long activeShipments;
        private long deliveredShipments;
        private long cancelledShipments;
        private long pendingShipments;
    }

    @Data @Builder
    public static class DeliveryPerformance {
        private long deliveredOnTime;
        private long deliveredLate;
        private double onTimeDeliveryRate;
        private double averageDeliveryTimeHours;
    }

    @Data @Builder
    public static class DelayAnalysis {
        private long delayedShipments;
        private double delayedPercentage;
        private long overdueShipments;
    }

    @Data @Builder
    public static class CustomerActivity {
        private long uniqueCustomers;
        private long shipmentsWithReceiverEmail;
        private Map<String, Long> shipmentsByReceiver;
    }

    @Data @Builder
    public static class LogisticsOverview {
        private Map<String, Long> shipmentCountByStatus;
        private long totalRoutes;
        private long activeRoutes;
        private double totalRouteDistanceKm;
        private long completedRoutes;
    }
}
