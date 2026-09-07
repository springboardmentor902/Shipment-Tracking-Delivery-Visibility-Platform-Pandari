package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.CreateRouteRequest;
import com.shiptrack.shiptrack_pro.dto.LocationUpdateRequest;
import com.shiptrack.shiptrack_pro.dto.LocationUpdateResponse;
import com.shiptrack.shiptrack_pro.dto.RouteResponse;
import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.RouteRepository;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.RouteService;
import com.shiptrack.shiptrack_pro.service.integration.MapRoutingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final MapRoutingService mapRoutingService;
    private final SimpMessagingTemplate messagingTemplate;


    // =========================================================
    // CREATE ROUTE
    // =========================================================

    @Override
    public RouteResponse createRoute(CreateRouteRequest request) {

        log.info("Creating route...");

        // Validate driver
        if (request.getDriverId() == null) {
            throw new RuntimeException("Driver ID is required");
        }

        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Driver not found: " + request.getDriverId()
                        )
                );


        // Validate shipments
        List<Long> shipmentIds = request.getShipmentIds();

        if (shipmentIds == null || shipmentIds.isEmpty()) {
            throw new RuntimeException(
                    "At least one shipment is required"
            );
        }


        // Currently Route entity contains one shipment
        Long shipmentId = shipmentIds.get(0);

        if (shipmentId == null) {
            throw new RuntimeException(
                    "Shipment ID cannot be null"
            );
        }

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Shipment not found: " + shipmentId
                        )
                );


        // Validate origin and destination
        if (request.getOrigin() == null ||
                request.getOrigin().isBlank()) {

            throw new RuntimeException(
                    "Origin is required"
            );
        }

        if (request.getDestination() == null ||
                request.getDestination().isBlank()) {

            throw new RuntimeException(
                    "Destination is required"
            );
        }


        // =====================================================
        // OSRM ROUTING
        // =====================================================

        Double distanceKm = null;
        Integer estimatedTimeMinutes = null;

        try {

            distanceKm = mapRoutingService.getDistance(
                    request.getOrigin(),
                    request.getDestination()
            );

            estimatedTimeMinutes =
                    mapRoutingService.getEstimatedTime(
                            request.getOrigin(),
                            request.getDestination()
                    );

            log.info(
                    "Route calculated: {} km, {} minutes",
                    distanceKm,
                    estimatedTimeMinutes
            );

        } catch (Exception e) {

            log.error(
                    "Error calculating route using OSRM: {}",
                    e.getMessage(),
                    e
            );
        }


        // =====================================================
        // CREATE ROUTE ENTITY
        // =====================================================

        Route route = Route.builder()

                .driver(driver)

                .shipment(shipment)

                .origin(request.getOrigin())

                .destination(request.getDestination())

                .distanceKm(distanceKm)

                .estimatedTimeMinutes(
                        estimatedTimeMinutes
                )

                .actualTimeMinutes(null)

                .trafficCondition("UNKNOWN")

                .routeStatus("PLANNED")

                .totalShipments(
                        shipmentIds.size()
                )

                .deliveredCount(0)

                // Initial driver location
                .currentLatitude(null)
                .currentLongitude(null)

                .build();


        Route savedRoute =
                routeRepository.save(route);


        log.info(
                "Route created successfully. Route ID: {}",
                savedRoute.getId()
        );


        return convertToResponse(savedRoute);
    }


    // =========================================================
    // UPDATE ROUTE
    // =========================================================

    @Override
    public RouteResponse updateRoute(
            Long routeId,
            CreateRouteRequest request) {

        log.info(
                "Updating route: {}",
                routeId
        );

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Route not found: " + routeId
                        )
                );


        // -----------------------------------------------------
        // Update driver
        // -----------------------------------------------------

        if (request.getDriverId() != null) {

            User driver =
                    userRepository.findById(
                            request.getDriverId()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Driver not found: "
                                            + request.getDriverId()
                            )
                    );

            route.setDriver(driver);
        }


        // -----------------------------------------------------
        // Update origin
        // -----------------------------------------------------

        if (request.getOrigin() != null &&
                !request.getOrigin().isBlank()) {

            route.setOrigin(
                    request.getOrigin()
            );
        }


        // -----------------------------------------------------
        // Update destination
        // -----------------------------------------------------

        if (request.getDestination() != null &&
                !request.getDestination().isBlank()) {

            route.setDestination(
                    request.getDestination()
            );
        }


        // -----------------------------------------------------
        // Recalculate route
        // -----------------------------------------------------

        if (route.getOrigin() != null &&
                route.getDestination() != null) {

            try {

                Double distanceKm =
                        mapRoutingService.getDistance(
                                route.getOrigin(),
                                route.getDestination()
                        );

                Integer estimatedTimeMinutes =
                        mapRoutingService.getEstimatedTime(
                                route.getOrigin(),
                                route.getDestination()
                        );

                route.setDistanceKm(
                        distanceKm
                );

                route.setEstimatedTimeMinutes(
                        estimatedTimeMinutes
                );

                log.info(
                        "Route recalculated: {} km, {} minutes",
                        distanceKm,
                        estimatedTimeMinutes
                );

            } catch (Exception e) {

                log.error(
                        "Error recalculating route: {}",
                        e.getMessage(),
                        e
                );
            }
        }


        Route updatedRoute =
                routeRepository.save(route);


        log.info(
                "Route updated successfully: {}",
                routeId
        );


        return convertToResponse(updatedRoute);
    }


    // =========================================================
    // GET ROUTE BY SHIPMENT
    // =========================================================

    @Override
    public RouteResponse getRouteByShipment(
            Long shipmentId) {

        log.info(
                "Getting route for shipment: {}",
                shipmentId
        );

        Route route = routeRepository
                .findTopByShipment_IdOrderByCreatedAtDesc(shipmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Route not found for shipment: "
                                        + shipmentId
                        )
                );


        return convertToResponse(route);
    }


    // =========================================================
    // UPDATE DRIVER LOCATION
    // =========================================================

    @Override
    public LocationUpdateResponse updateLocation(
            Long routeId,
            LocationUpdateRequest request) {

        log.info(
                "Updating location for route: {}",
                routeId
        );


        // -----------------------------------------------------
        // Find route
        // -----------------------------------------------------

        Route route =
                routeRepository.findById(routeId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Route not found: "
                                                + routeId
                                )
                        );


        // -----------------------------------------------------
        // Validate coordinates
        // -----------------------------------------------------

        if (request == null) {

            throw new RuntimeException(
                    "Location request cannot be null"
            );
        }


        if (request.getLatitude() == null) {

            throw new RuntimeException(
                    "Latitude is required"
            );
        }


        if (request.getLongitude() == null) {

            throw new RuntimeException(
                    "Longitude is required"
            );
        }


        if (request.getLatitude() < -90 ||
                request.getLatitude() > 90) {

            throw new RuntimeException(
                    "Invalid latitude. "
                            + "Latitude must be between -90 and 90"
            );
        }


        if (request.getLongitude() < -180 ||
                request.getLongitude() > 180) {

            throw new RuntimeException(
                    "Invalid longitude. "
                            + "Longitude must be between -180 and 180"
            );
        }


        // -----------------------------------------------------
        // Save current location
        // -----------------------------------------------------

        route.setCurrentLatitude(
                request.getLatitude()
        );

        route.setCurrentLongitude(
                request.getLongitude()
        );


        Route savedRoute =
                routeRepository.save(route);


        log.info(
                "Location saved. Route: {}, Latitude: {}, Longitude: {}",
                routeId,
                savedRoute.getCurrentLatitude(),
                savedRoute.getCurrentLongitude()
        );


        // -----------------------------------------------------
        // Get shipment ID
        // -----------------------------------------------------

        Long shipmentId = null;

        if (savedRoute.getShipment() != null) {

            shipmentId =
                    savedRoute.getShipment().getId();
        }


        // -----------------------------------------------------
        // Build response
        // -----------------------------------------------------

        LocationUpdateResponse response =
                LocationUpdateResponse.builder()

                        .routeId(
                                savedRoute.getId()
                        )

                        .shipmentId(
                                shipmentId
                        )

                        .latitude(
                                savedRoute
                                        .getCurrentLatitude()
                        )

                        .longitude(
                                savedRoute
                                        .getCurrentLongitude()
                        )

                        .timestamp(
                                LocalDateTime.now()
                        )

                        .build();


        // -----------------------------------------------------
        // Broadcast location using STOMP
        // -----------------------------------------------------

        if (shipmentId != null) {

            String destination =
                    "/topic/shipments/"
                            + shipmentId
                            + "/location";


            messagingTemplate.convertAndSend(
                    destination,
                    response
            );


            log.info(
                    "Location broadcasted to: {}",
                    destination
            );

        } else {

            log.warn(
                    "No shipment associated with route {}. "
                            + "Location was saved but not broadcast.",
                    routeId
            );
        }


        return response;
    }


    // =========================================================
    // CONVERT ROUTE ENTITY TO RESPONSE
    // =========================================================

    private RouteResponse convertToResponse(
            Route route) {

        return RouteResponse.builder()

                .id(
                        route.getId()
                )

                .driverName(
                        route.getDriver() != null
                                ? route.getDriver()
                                .getFullName()
                                : null
                )

                .routeStatus(
                        route.getRouteStatus()
                )

                .origin(
                        route.getOrigin()
                )

                .destination(
                        route.getDestination()
                )

                .distanceKm(
                        route.getDistanceKm()
                )

                .estimatedTimeMinutes(
                        route.getEstimatedTimeMinutes()
                )

                .actualTimeMinutes(
                        route.getActualTimeMinutes()
                )

                .trafficCondition(
                        route.getTrafficCondition()
                )

                .totalShipments(
                        route.getTotalShipments()
                )

                .deliveredCount(
                        route.getDeliveredCount()
                )

                .currentLatitude(
                        route.getCurrentLatitude()
                )

                .currentLongitude(
                        route.getCurrentLongitude()
                )

                .startedAt(
                        route.getStartedAt()
                )

                .completedAt(
                        route.getCompletedAt()
                )

                .build();
    }
}