package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.ETAPredictionResponse;
import com.shiptrack.shiptrack_pro.entity.ETAPrediction;
import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.TrackingEvent;
import com.shiptrack.shiptrack_pro.repository.ETAPredictionRepository;
import com.shiptrack.shiptrack_pro.repository.RouteRepository;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.TrackingEventRepository;
import com.shiptrack.shiptrack_pro.service.ETAPredictionService;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ETAPredictionServiceImpl
        implements ETAPredictionService {

    private final ETAPredictionRepository etaRepository;
    private final ShipmentRepository shipmentRepository;
    private final RouteRepository routeRepository;
    private final TrackingEventRepository trackingRepository;

    public ETAPredictionServiceImpl(
            ETAPredictionRepository etaRepository,
            ShipmentRepository shipmentRepository,
            RouteRepository routeRepository,
            TrackingEventRepository trackingRepository) {

        this.etaRepository = etaRepository;
        this.shipmentRepository = shipmentRepository;
        this.routeRepository = routeRepository;
        this.trackingRepository = trackingRepository;
    }

    // =========================================================
    // PREDICT / RECALCULATE ETA
    // =========================================================

    @Override
    public ETAPredictionResponse predictETA(Long shipmentId) {

        // -----------------------------------------------------
        // 1. Find shipment
        // -----------------------------------------------------

        Shipment shipment = shipmentRepository
                .findById(shipmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Shipment not found: " + shipmentId
                        )
                );

        // -----------------------------------------------------
        // 2. Find route
        // -----------------------------------------------------

        Route route = routeRepository
                .findTopByShipment_IdOrderByCreatedAtDesc(shipmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Route not found for shipment: "
                                        + shipmentId
                        )
                );

        // -----------------------------------------------------
        // 3. Get tracking history
        // -----------------------------------------------------

        List<TrackingEvent> events =
                trackingRepository
                        .getShipmentTrackingHistory(shipmentId);

        // -----------------------------------------------------
        // 4. Basic ETA information from route
        // -----------------------------------------------------

        int estimatedMinutes =
                route.getEstimatedTimeMinutes();

        int risk = 0;
        int confidence = 80;

        StringBuilder factors =
                new StringBuilder();

        // -----------------------------------------------------
        // 5. Check traffic condition
        // -----------------------------------------------------

        String traffic =
                route.getTrafficCondition();

        if (traffic != null) {

            switch (traffic.toUpperCase()) {

                case "MODERATE":

                    risk += 2;

                    factors.append(
                            "Moderate traffic; "
                    );

                    break;

                case "HIGH":

                    risk += 4;
                    confidence -= 5;

                    factors.append(
                            "High traffic; "
                    );

                    break;

                case "SEVERE":

                    risk += 6;
                    confidence -= 10;

                    factors.append(
                            "Severe traffic; "
                    );

                    break;

                default:

                    factors.append(
                            "Low traffic; "
                    );

                    break;
            }
        }

        // -----------------------------------------------------
        // 6. Analyze tracking history
        // -----------------------------------------------------

        if (events.isEmpty()) {

            risk += 2;
            confidence -= 20;

            factors.append(
                    "No tracking history available; "
            );

        } else {

            confidence += 10;

            factors.append(
                    "Recent tracking data available; "
            );

            // -------------------------------------------------
            // Latest tracking event
            // -------------------------------------------------

            TrackingEvent latestEvent =
                    events.get(0);

            String status =
                    latestEvent.getStatus();

            if (status != null) {

                if ("EXCEPTION"
                        .equalsIgnoreCase(status)) {

                    risk += 4;
                    confidence -= 10;

                    factors.append(
                            "Shipment has an exception; "
                    );

                } else if ("OUT_FOR_DELIVERY"
                        .equalsIgnoreCase(status)) {

                    risk = Math.max(
                            0,
                            risk - 1
                    );

                    factors.append(
                            "Shipment is out for delivery; "
                    );

                } else if ("IN_TRANSIT"
                        .equalsIgnoreCase(status)) {

                    factors.append(
                            "Shipment is in transit; "
                    );

                } else if ("DELIVERED"
                        .equalsIgnoreCase(status)) {

                    risk = 0;

                    factors.append(
                            "Shipment is delivered; "
                    );
                }
            }

            // -------------------------------------------------
            // Check how old the latest tracking event is
            // -------------------------------------------------

            if (latestEvent.getEventTimestamp() != null) {

                long minutesSinceUpdate =
                        Duration.between(
                                latestEvent.getEventTimestamp(),
                                LocalDateTime.now()
                        ).toMinutes();

                if (minutesSinceUpdate > 60) {

                    risk += 2;
                    confidence -= 15;

                    factors.append(
                            "No tracking update for over 60 minutes; "
                    );

                } else {

                    factors.append(
                            "Tracking update is recent; "
                    );
                }
            }
        }

        // -----------------------------------------------------
        // 7. Keep risk between 0 and 10
        // -----------------------------------------------------

        risk = Math.max(
                0,
                Math.min(10, risk)
        );

        // -----------------------------------------------------
        // 8. Keep confidence between 0 and 100
        // -----------------------------------------------------

        confidence = Math.max(
                0,
                Math.min(100, confidence)
        );

        // -----------------------------------------------------
        // 9. Calculate predicted delivery time
        // -----------------------------------------------------

        LocalDateTime calculatedAt =
                LocalDateTime.now();

        LocalDateTime predictedDelivery =
                calculatedAt.plusMinutes(
                        estimatedMinutes
                );

        // -----------------------------------------------------
        // 10. Find existing prediction
        //     or create a new one
        // -----------------------------------------------------

        ETAPrediction prediction =
                etaRepository
                        .findByShipment_Id(shipmentId)
                        .orElse(
                                new ETAPrediction()
                        );

        // -----------------------------------------------------
        // 11. Set prediction values
        // -----------------------------------------------------

        prediction.setShipment(
                shipment
        );

        prediction.setPredictedDeliveryTime(
                predictedDelivery
        );

        prediction.setDelayRiskScore(
                (double) risk
        );

        prediction.setConfidenceScore(
                (double) confidence
        );

        prediction.setFactors(
                factors.toString()
        );

        prediction.setCalculatedAt(
                calculatedAt
        );

        // -----------------------------------------------------
        // 12. Save prediction
        // -----------------------------------------------------

        ETAPrediction savedPrediction =
                etaRepository.save(
                        prediction
                );

        // -----------------------------------------------------
        // 13. Return response
        // -----------------------------------------------------

        return convertToResponse(
                savedPrediction
        );
    }

    // =========================================================
    // GET EXISTING ETA
    // =========================================================

    @Override
    public ETAPredictionResponse getPrediction(
            Long shipmentId) {

        ETAPrediction prediction =
                etaRepository
                        .findByShipment_Id(shipmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "ETA prediction not found for shipment: "
                                                + shipmentId
                                )
                        );

        return convertToResponse(
                prediction
        );
    }

    // =========================================================
    // GET AT-RISK SHIPMENTS
    // =========================================================

    @Override
    public List<ETAPredictionResponse>
    getAtRiskShipments() {

        return etaRepository
                .findByDelayRiskScoreGreaterThan(6.0)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // =========================================================
    // CONVERT ENTITY TO DTO
    // =========================================================

    private ETAPredictionResponse convertToResponse(
            ETAPrediction prediction) {

        ETAPredictionResponse response =
                new ETAPredictionResponse();

        response.setShipmentId(
                prediction
                        .getShipment()
                        .getId()
        );

        response.setPredictedDeliveryTime(
                prediction
                        .getPredictedDeliveryTime()
        );

        response.setDelayRiskScore(
                prediction
                        .getDelayRiskScore()
        );

        response.setConfidenceScore(
                prediction
                        .getConfidenceScore()
        );

        response.setFactors(
                prediction
                        .getFactors()
        );

        response.setCalculatedAt(
                prediction
                        .getCalculatedAt()
        );

        return response;
    }
}



