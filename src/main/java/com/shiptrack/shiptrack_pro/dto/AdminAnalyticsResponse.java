package com.shiptrack.shiptrack_pro.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AdminAnalyticsResponse {
    private UserSummary userSummary;
    private ShipmentMonitoring platformShipmentMonitoring;
    private DeliveryAnalytics deliveryAnalytics;
    private RoutePerformance routePerformance;
    private SystemMonitoring systemMonitoring;
    private ReportsManagement reportsManagement;

    @Data @Builder
    public static class UserSummary {
        private long totalUsers;
        private Map<String, Long> usersByRole;
        private Map<String, Long> usersByStatus;
        private long activeUsers;
    }

    @Data @Builder
    public static class ShipmentMonitoring {
        private long totalShipments;
        private Map<String, Long> shipmentCountByStatus;
        private long activeShipments;
        private long deliveredShipments;
        private long delayedShipments;
        private long cancelledShipments;
    }

    @Data @Builder
    public static class DeliveryAnalytics {
        private long deliveredOnTime;
        private long deliveredLate;
        private double onTimeDeliveryRate;
        private double averageDeliveryTimeHours;
    }

    @Data @Builder
    public static class RoutePerformance {
        private long totalRoutes;
        private long activeRoutes;
        private long completedRoutes;
        private double totalDistanceKm;
        private double averageRouteDistanceKm;
        private double averagePlannedTimeMinutes;
        private double averageActualTimeMinutes;
    }

    @Data @Builder
    public static class SystemMonitoring {
        private long registeredUsers;
        private long totalShipments;
        private long totalRoutes;
        private long shipmentsCreatedLast24Hours;
        private long usersCreatedLast24Hours;
        private long routesCreatedLast24Hours;
    }

    @Data @Builder
    public static class ReportsManagement {
        private long totalShipmentsAvailableForReporting;
        private long totalRoutesAvailableForReporting;
        private long totalUsersAvailableForReporting;
        private String reportScope;
    }
}
