package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.CreateShipmentRequest;
import com.shiptrack.shiptrack_pro.dto.ShipmentResponse;
import com.shiptrack.shiptrack_pro.dto.TrackingEventResponse;
import com.shiptrack.shiptrack_pro.dto.UpdateShipmentStatusRequest;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.security.JwtUtil;
import com.shiptrack.shiptrack_pro.service.ShipmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@Slf4j
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * Create a new shipment
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BUSINESS_CLIENT', 'OPERATOR')")
    public ResponseEntity<?> createShipment(
            @Valid @RequestBody CreateShipmentRequest request,
            @RequestHeader("Authorization") String token) {

        try {
            Long userId = extractUserIdFromToken(token);

            ShipmentResponse response =
                    shipmentService.createShipment(request, userId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (Exception e) {
            log.error("Error creating shipment: {}", e.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get shipment details by tracking number
     */
    @GetMapping("/{trackingNumber}")
    public ResponseEntity<?> getShipment(
            @PathVariable String trackingNumber) {

        try {
            ShipmentResponse response =
                    shipmentService.getShipment(trackingNumber);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching shipment: {}", e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Shipment not found"));
        }
    }

    /**
     * Get tracking history for a shipment
     */
    @GetMapping("/{trackingNumber}/tracking")
    public ResponseEntity<?> getTrackingHistory(
            @PathVariable String trackingNumber) {

        try {
            List<TrackingEventResponse> history =
                    shipmentService.getTrackingHistory(trackingNumber);

            return ResponseEntity.ok(
                    Map.of(
                            "tracking_number", trackingNumber,
                            "events", history,
                            "total_events", history.size()
                    )
            );

        } catch (Exception e) {
            log.error("Error fetching tracking history: {}", e.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update shipment status
     * Only OPERATOR and ADMIN can update shipment status
     */
    @PostMapping("/{trackingNumber}/status")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<?> updateShipmentStatus(
            @PathVariable String trackingNumber,
            @Valid @RequestBody UpdateShipmentStatusRequest request,
            @RequestHeader("Authorization") String token) {

        try {
            request.setTrackingNumber(trackingNumber);

            Long userId = extractUserIdFromToken(token);

            TrackingEventResponse response =
                    shipmentService.addTrackingEvent(request, userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating shipment status: {}", e.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get shipments belonging to the currently logged-in user
     */
    @GetMapping("/user/my-shipments")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BUSINESS_CLIENT')")
    public ResponseEntity<?> getUserShipments(
            @RequestHeader("Authorization") String token) {

        try {
            Long userId = extractUserIdFromToken(token);

            List<ShipmentResponse> shipments =
                    shipmentService.getUserShipments(userId);

            return ResponseEntity.ok(
                    Map.of(
                            "total", shipments.size(),
                            "shipments", shipments
                    )
            );

        } catch (Exception e) {
            log.error("Error fetching user shipments: {}", e.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get shipments by status
     * Only ADMIN can access this endpoint
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getShipmentsByStatus(
            @PathVariable String status) {

        try {
            List<ShipmentResponse> shipments =
                    shipmentService.getShipmentsByStatus(status);

            return ResponseEntity.ok(
                    Map.of(
                            "status", status,
                            "total", shipments.size(),
                            "shipments", shipments
                    )
            );

        } catch (Exception e) {
            log.error("Error fetching shipments by status: {}", e.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cancel a shipment
     */
    @PostMapping("/{trackingNumber}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BUSINESS_CLIENT', 'ADMIN')")
    public ResponseEntity<?> cancelShipment(
            @PathVariable String trackingNumber,
            @RequestBody Map<String, String> payload) {

        try {
            String reason =
                    payload.getOrDefault("reason", "No reason provided");

            ShipmentResponse response =
                    shipmentService.cancelShipment(
                            trackingNumber,
                            reason
                    );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error cancelling shipment: {}", e.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a shipment
     */
    @DeleteMapping("/{trackingNumber}")
    @PreAuthorize("hasAnyRole('BUSINESS_CLIENT', 'ADMIN')")
    public ResponseEntity<?> deleteShipment(
            @PathVariable String trackingNumber) {

        try {
            shipmentService.deleteShipment(trackingNumber);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Shipment deleted successfully"
                    )
            );

        } catch (Exception e) {
            log.error("Error deleting shipment: {}", e.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Extract the actual logged-in user's ID from JWT token
     */
    private Long extractUserIdFromToken(String token) {

        // Remove "Bearer " from Authorization header
        String jwt = token.replace("Bearer ", "");

        // Extract email from JWT
        String email = jwtUtil.extractEmail(jwt);

        // Find user using email
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                )
                .getId();
    }
}
