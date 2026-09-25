package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.AdminAnalyticsResponse;
import com.shiptrack.shiptrack_pro.dto.BusinessAnalyticsResponse;
import com.shiptrack.shiptrack_pro.dto.CustomerAnalyticsResponse;
import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.AnalyticsRouteRepository;
import com.shiptrack.shiptrack_pro.repository.AnalyticsShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.RouteRepository;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsShipmentRepository shipmentRepository;
    private final AnalyticsRouteRepository routeRepository;
    private final UserRepository userRepository;

    @Override
    @Cacheable(value = "customerAnalytics", key = "#userId")
    public CustomerAnalyticsResponse getCustomerAnalytics(Long userId) {
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        List<Shipment> shipments = shipmentRepository.findByCreatedBy_Id(userId);
        List<Route> routes = routeRepository.findByShipment_CreatedBy_Id(userId);

        Map<String, Long> statusBreakdown = shipments.stream()
                .collect(Collectors.groupingBy(s -> normalize(s.getStatus()), Collectors.counting()));

        long delivered = countStatus(shipments, "DELIVERED");
        long inTransit = countStatus(shipments, "IN_TRANSIT", "IN TRANSIT");
        long cancelled = countStatus(shipments, "CANCELLED", "CANCELED");
        long delayed = shipments.stream().filter(this::isDelayed).count();

        long active = shipments.stream()
                .filter(s -> !isStatus(s, "DELIVERED", "CANCELLED", "CANCELED"))
                .count();

        long activeRoutes = routes.stream()
                .filter(r -> !isStatus(r.getRouteStatus(), "COMPLETED", "CANCELLED", "CANCELED"))
                .count();

        List<CustomerAnalyticsResponse.ShipmentHistoryItem> history = shipments.stream()
                .sorted(Comparator.comparing(
                        Shipment::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(s -> CustomerAnalyticsResponse.ShipmentHistoryItem.builder()
                        .shipmentId(s.getId())
                        .trackingNumber(s.getTrackingNumber())
                        .status(s.getStatus())
                        .priority(s.getPriority())
                        .estimatedDeliveryDate(string(s.getEstimatedDeliveryDate()))
                        .actualDeliveryDate(string(s.getActualDeliveryDate()))
                        .createdAt(string(s.getCreatedAt()))
                        .build())
                .toList();

        return CustomerAnalyticsResponse.builder()
                .customerId(customer.getId())
                .customerName(customer.getFullName())
                .activeShipmentCount((int) active)
                .totalShipmentCount(shipments.size())
                .shipmentStatusBreakdown(statusBreakdown)
                .shipmentHistory(history)
                .trackingInsights(CustomerAnalyticsResponse.TrackingInsights.builder()
                        .deliveredCount(delivered)
                        .inTransitCount(inTransit)
                        .delayedCount(delayed)
                        .cancelledCount(cancelled)
                        .routesTracked(routes.size())
                        .activeRoutes(activeRoutes)
                        .build())
                .build();
    }

    @Override
    @Cacheable(value = "businessAnalytics", key = "#businessId")
    public BusinessAnalyticsResponse getBusinessAnalytics(Long businessId) {
        User business = userRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business client not found"));

        // Business ownership is based on Shipment.businessId, as defined in Shipment.java.
        List<Shipment> shipments = shipmentRepository.findByBusinessId(businessId);
        List<Route> routes = routeRepository.findByShipment_BusinessId(businessId);

        long delivered = countStatus(shipments, "DELIVERED");
        long cancelled = countStatus(shipments, "CANCELLED", "CANCELED");
        long active = shipments.stream()
                .filter(s -> !isStatus(s, "DELIVERED", "CANCELLED", "CANCELED"))
                .count();

        long onTime = 0;
        long late = 0;
        double deliveryHoursTotal = 0;
        int deliverySamples = 0;

        for (Shipment s : shipments) {
            if (s.getActualDeliveryDate() != null) {
                if (s.getEstimatedDeliveryDate() != null
                        && !s.getActualDeliveryDate().isAfter(s.getEstimatedDeliveryDate())) {
                    onTime++;
                } else if (s.getEstimatedDeliveryDate() != null) {
                    late++;
                }
                if (s.getCreatedAt() != null) {
                    deliveryHoursTotal += Duration.between(
                            s.getCreatedAt(), s.getActualDeliveryDate()).toMinutes() / 60.0;
                    deliverySamples++;
                }
            }
        }

        long delayed = shipments.stream().filter(this::isDelayed).count();
        long overdue = shipments.stream()
                .filter(s -> s.getActualDeliveryDate() == null
                        && s.getEstimatedDeliveryDate() != null
                        && s.getEstimatedDeliveryDate().isBefore(LocalDateTime.now()))
                .count();

        Map<String, Long> receiverActivity = shipments.stream()
                .filter(s -> s.getReceiverEmail() != null && !s.getReceiverEmail().isBlank())
                .collect(Collectors.groupingBy(
                        Shipment::getReceiverEmail, TreeMap::new, Collectors.counting()));

        double totalDistance = routes.stream()
                .filter(r -> r.getDistanceKm() != null)
                .mapToDouble(Route::getDistanceKm).sum();

        return BusinessAnalyticsResponse.builder()
                .businessId(businessId)
                .businessName(business.getFullName())
                .shipmentAnalytics(BusinessAnalyticsResponse.ShipmentAnalytics.builder()
                        .totalShipments(shipments.size())
                        .activeShipments(active)
                        .deliveredShipments(delivered)
                        .cancelledShipments(cancelled)
                        .pendingShipments(countStatus(shipments, "PENDING"))
                        .build())
                .deliveryPerformance(BusinessAnalyticsResponse.DeliveryPerformance.builder()
                        .deliveredOnTime(onTime)
                        .deliveredLate(late)
                        .onTimeDeliveryRate(rate(onTime, onTime + late))
                        .averageDeliveryTimeHours(deliverySamples == 0 ? 0 : deliveryHoursTotal / deliverySamples)
                        .build())
                .delayAnalysis(BusinessAnalyticsResponse.DelayAnalysis.builder()
                        .delayedShipments(delayed)
                        .delayedPercentage(rate(delayed, shipments.size()))
                        .overdueShipments(overdue)
                        .build())
                .customerActivity(BusinessAnalyticsResponse.CustomerActivity.builder()
                        .uniqueCustomers(receiverActivity.size())
                        .shipmentsWithReceiverEmail(receiverActivity.values().stream().mapToLong(Long::longValue).sum())
                        .shipmentsByReceiver(receiverActivity)
                        .build())
                .logisticsOverview(BusinessAnalyticsResponse.LogisticsOverview.builder()
                        .shipmentCountByStatus(groupByStatus(shipments))
                        .totalRoutes(routes.size())
                        .activeRoutes(routes.stream().filter(r -> !isStatus(r.getRouteStatus(), "COMPLETED", "CANCELLED", "CANCELED")).count())
                        .totalRouteDistanceKm(totalDistance)
                        .completedRoutes(routes.stream().filter(r -> isStatus(r.getRouteStatus(), "COMPLETED")).count())
                        .build())
                .build();
    }

    @Override
    @Cacheable(value = "adminAnalytics", key = "'platform'")
    public AdminAnalyticsResponse getAdminAnalytics() {
        List<User> users = userRepository.findAll();
        List<Shipment> shipments = shipmentRepository.findAll();
        List<Route> routes = routeRepository.findAll();

        Map<String, Long> usersByRole = users.stream()
                .collect(Collectors.groupingBy(u -> normalize(u.getRole()), Collectors.counting()));
        Map<String, Long> usersByStatus = users.stream()
                .collect(Collectors.groupingBy(u -> normalize(u.getStatus()), Collectors.counting()));

        long delivered = countStatus(shipments, "DELIVERED");
        long cancelled = countStatus(shipments, "CANCELLED", "CANCELED");
        long delayed = shipments.stream().filter(this::isDelayed).count();

        long onTime = 0, late = 0;
        double deliveryHours = 0;
        int samples = 0;
        for (Shipment s : shipments) {
            if (s.getActualDeliveryDate() != null && s.getEstimatedDeliveryDate() != null) {
                if (!s.getActualDeliveryDate().isAfter(s.getEstimatedDeliveryDate())) onTime++;
                else late++;
            }
            if (s.getActualDeliveryDate() != null && s.getCreatedAt() != null) {
                deliveryHours += Duration.between(s.getCreatedAt(), s.getActualDeliveryDate()).toMinutes() / 60.0;
                samples++;
            }
        }

        double totalDistance = routes.stream()
                .filter(r -> r.getDistanceKm() != null)
                .mapToDouble(Route::getDistanceKm).sum();

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        long recentUsers = users.stream().filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(since)).count();
        long recentShipments = shipments.stream().filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isAfter(since)).count();
        long recentRoutes = routes.stream().filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(since)).count();

        return AdminAnalyticsResponse.builder()
                .userSummary(AdminAnalyticsResponse.UserSummary.builder()
                        .totalUsers(users.size())
                        .usersByRole(usersByRole)
                        .usersByStatus(usersByStatus)
                        .activeUsers(users.stream().filter(u -> isStatus(u.getStatus(), "ACTIVE")).count())
                        .build())
                .platformShipmentMonitoring(AdminAnalyticsResponse.ShipmentMonitoring.builder()
                        .totalShipments(shipments.size())
                        .shipmentCountByStatus(groupByStatus(shipments))
                        .activeShipments(shipments.stream().filter(s -> !isStatus(s, "DELIVERED", "CANCELLED", "CANCELED")).count())
                        .deliveredShipments(delivered)
                        .delayedShipments(delayed)
                        .cancelledShipments(cancelled)
                        .build())
                .deliveryAnalytics(AdminAnalyticsResponse.DeliveryAnalytics.builder()
                        .deliveredOnTime(onTime)
                        .deliveredLate(late)
                        .onTimeDeliveryRate(rate(onTime, onTime + late))
                        .averageDeliveryTimeHours(samples == 0 ? 0 : deliveryHours / samples)
                        .build())
                .routePerformance(AdminAnalyticsResponse.RoutePerformance.builder()
                        .totalRoutes(routes.size())
                        .activeRoutes(routes.stream().filter(r -> !isStatus(r.getRouteStatus(), "COMPLETED", "CANCELLED", "CANCELED")).count())
                        .completedRoutes(routes.stream().filter(r -> isStatus(r.getRouteStatus(), "COMPLETED")).count())
                        .totalDistanceKm(totalDistance)
                        .averageRouteDistanceKm(routes.isEmpty() ? 0 : totalDistance / routes.size())
                        .averagePlannedTimeMinutes(averageInt(routes.stream().map(Route::getEstimatedTimeMinutes).toList()))
                        .averageActualTimeMinutes(averageInt(routes.stream().map(Route::getActualTimeMinutes).toList()))
                        .build())
                .systemMonitoring(AdminAnalyticsResponse.SystemMonitoring.builder()
                        .registeredUsers(users.size())
                        .totalShipments(shipments.size())
                        .totalRoutes(routes.size())
                        .shipmentsCreatedLast24Hours(recentShipments)
                        .usersCreatedLast24Hours(recentUsers)
                        .routesCreatedLast24Hours(recentRoutes)
                        .build())
                .reportsManagement(AdminAnalyticsResponse.ReportsManagement.builder()
                        .totalShipmentsAvailableForReporting(shipments.size())
                        .totalRoutesAvailableForReporting(routes.size())
                        .totalUsersAvailableForReporting(users.size())
                        .reportScope("PLATFORM_WIDE")
                        .build())
                .build();
    }

    private Map<String, Long> groupByStatus(List<Shipment> shipments) {
        return shipments.stream()
                .collect(Collectors.groupingBy(s -> normalize(s.getStatus()), TreeMap::new, Collectors.counting()));
    }

    private long countStatus(List<Shipment> shipments, String... statuses) {
        return shipments.stream().filter(s -> isStatus(s, statuses)).count();
    }

    private boolean isStatus(Shipment s, String... statuses) {
        return isStatus(s.getStatus(), statuses);
    }

    private boolean isStatus(String actual, String... statuses) {
        if (actual == null) return false;
        String a = normalize(actual);
        return Arrays.stream(statuses).anyMatch(x -> normalize(x).equals(a));
    }

    private boolean isDelayed(Shipment s) {
        if (isStatus(s, "CANCELLED", "CANCELED")) return false;
        if (s.getActualDeliveryDate() != null && s.getEstimatedDeliveryDate() != null) {
            return s.getActualDeliveryDate().isAfter(s.getEstimatedDeliveryDate());
        }
        return s.getActualDeliveryDate() == null
                && s.getEstimatedDeliveryDate() != null
                && s.getEstimatedDeliveryDate().isBefore(LocalDateTime.now());
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) return "UNKNOWN";
        return value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private String string(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    private double rate(long numerator, long denominator) {
        return denominator == 0 ? 0 : Math.round((numerator * 10000.0 / denominator)) / 100.0;
    }

    private double averageInt(List<Integer> values) {
        return values.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).average().orElse(0);
    }
}
