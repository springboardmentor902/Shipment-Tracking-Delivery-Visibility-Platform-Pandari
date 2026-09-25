package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.CreateRouteRequest;
import com.shiptrack.shiptrack_pro.dto.LocationUpdateRequest;
import com.shiptrack.shiptrack_pro.dto.LocationUpdateResponse;
import com.shiptrack.shiptrack_pro.dto.RouteResponse;
import com.shiptrack.shiptrack_pro.service.RouteService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;


    // =========================================================
    // CREATE ROUTE
    // =========================================================

    /**
     * Create route
     *
     * Only LOGISTICS_OPERATOR or ADMINISTRATOR
     */
    @PostMapping
    @PreAuthorize(
            "hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')"
    )
    public ResponseEntity<?> createRoute(
            @Valid @RequestBody CreateRouteRequest request) {

        try {

            RouteResponse response =
                    routeService.createRoute(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // UPDATE ROUTE
    // =========================================================

    /**
     * Update route / change driver
     *
     * Only LOGISTICS_OPERATOR or ADMINISTRATOR
     */
    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('LOGISTICS_OPERATOR', 'ADMINISTRATOR')"
    )
    public ResponseEntity<?> updateRoute(
            @PathVariable Long id,
            @RequestBody CreateRouteRequest request) {

        try {

            RouteResponse response =
                    routeService.updateRoute(
                            id,
                            request
                    );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // GET ROUTE BY SHIPMENT
    // =========================================================

    /**
     * Get route for a shipment
     */
    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<?> getRouteByShipment(
            @PathVariable Long shipmentId) {

        try {

            RouteResponse response =
                    routeService.getRouteByShipment(
                            shipmentId
                    );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }


    // =========================================================
    // UPDATE DRIVER LOCATION
    // =========================================================

    /**
     * Driver sends current GPS location.
     *
     * The location is:
     *
     * 1. Saved in the Route table
     * 2. Broadcast using WebSocket/STOMP
     *
     * Only OPERATOR or ADMINISTRATOR
     */
    @PostMapping("/{routeId}/location")
    @PreAuthorize(
            "hasAnyRole('OPERATOR', 'LOGISTICS_OPERATOR', 'ADMINISTRATOR')"
    )
    public ResponseEntity<?> updateLocation(
            @PathVariable Long routeId,
            @Valid @RequestBody LocationUpdateRequest request) {

        try {

            LocationUpdateResponse response =
                    routeService.updateLocation(
                            routeId,
                            request
                    );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }
}
