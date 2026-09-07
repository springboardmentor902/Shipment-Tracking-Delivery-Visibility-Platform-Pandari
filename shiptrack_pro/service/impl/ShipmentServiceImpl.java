package com.shiptrack.shiptrack_pro.service.impl;

import com.google.gson.Gson;
import com.shiptrack.shiptrack_pro.dto.CreateShipmentRequest;
import com.shiptrack.shiptrack_pro.dto.ShipmentResponse;
import com.shiptrack.shiptrack_pro.dto.TrackingEventResponse;
import com.shiptrack.shiptrack_pro.dto.UpdateShipmentStatusRequest;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.TrackingEvent;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.TrackingEventRepository;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.ShipmentService;
import com.shiptrack.shiptrack_pro.service.integration.EmailService;
import com.shiptrack.shiptrack_pro.service.integration.MapRoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final UserRepository userRepository;

    private final MapRoutingService mapRoutingService;

    private final EmailService emailService;

    private final Gson gson;


    // ============================================================
    // CREATE SHIPMENT
    // ============================================================

    @Override
    public ShipmentResponse createShipment(
            CreateShipmentRequest request,
            Long userId
    ) {

        try {

            // ----------------------------------------------------
            // 1. Find user
            // ----------------------------------------------------

            User user = userRepository
                    .findById(userId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found"
                            )
                    );


            // ----------------------------------------------------
            // 2. Generate tracking number
            // ----------------------------------------------------

            String trackingNumber =
                    generateTrackingNumber();


            // ----------------------------------------------------
            // 3. Validate addresses
            // ----------------------------------------------------

            if (request.getSenderAddress() == null ||
                    request.getSenderAddress().isBlank()) {

                throw new RuntimeException(
                        "Sender address is required"
                );
            }

            if (request.getReceiverAddress() == null ||
                    request.getReceiverAddress().isBlank()) {

                throw new RuntimeException(
                        "Receiver address is required"
                );
            }


            // ----------------------------------------------------
            // 4. Geocode sender address
            //
            // Nominatim:
            // Address -> Latitude + Longitude
            // ----------------------------------------------------

            Map<String, Double> senderCoords =
                    mapRoutingService.geocodeAddress(
                            request.getSenderAddress()
                    );


            if (senderCoords == null) {

                throw new RuntimeException(
                        "Unable to find sender address: "
                                + request.getSenderAddress()
                );
            }


            // ----------------------------------------------------
            // 5. Geocode receiver address
            // ----------------------------------------------------

            Map<String, Double> receiverCoords =
                    mapRoutingService.geocodeAddress(
                            request.getReceiverAddress()
                    );


            if (receiverCoords == null) {

                throw new RuntimeException(
                        "Unable to find receiver address: "
                                + request.getReceiverAddress()
                );
            }


            // ----------------------------------------------------
            // 6. Calculate route
            //
            // OSRM:
            // Coordinates -> Distance + Duration
            //
            // We call getRoute() once instead of calling
            // getDistance() and getEstimatedTime() separately.
            // ----------------------------------------------------

            MapRoutingService.RouteResult routeResult =
                    mapRoutingService.getRoute(
                            request.getSenderAddress(),
                            request.getReceiverAddress()
                    );


            if (routeResult == null) {

                throw new RuntimeException(
                        "Unable to calculate route between sender and receiver"
                );
            }


            // ----------------------------------------------------
            // 7. Distance
            //
            // OSRM returns meters.
            // Shipment stores kilometers.
            // ----------------------------------------------------

            Double distanceKm =
                    routeResult.distanceMeters()
                            / 1000.0;


            // ----------------------------------------------------
            // 8. Estimated delivery time
            //
            // OSRM returns seconds.
            // Shipment uses minutes.
            // ----------------------------------------------------

            Integer estimatedTimeMinutes =
                    (int) Math.ceil(
                            routeResult.durationSeconds()
                                    / 60.0
                    );


            // ----------------------------------------------------
            // 9. Estimated delivery date
            // ----------------------------------------------------

            LocalDateTime estimatedDeliveryDate =
                    LocalDateTime.now()
                            .plusMinutes(
                                    estimatedTimeMinutes
                            );


            // ----------------------------------------------------
            // 10. Create shipment
            // ----------------------------------------------------

            Shipment shipment = Shipment.builder()

                    .trackingNumber(
                            trackingNumber
                    )

                    .createdBy(user)

                    // Sender
                    .senderName(
                            request.getSenderName()
                    )

                    .senderPhone(
                            request.getSenderPhone()
                    )

                    .senderEmail(
                            request.getSenderEmail()
                    )

                    .senderAddress(
                            request.getSenderAddress()
                    )

                    .senderCoordinates(
                            gson.toJson(senderCoords)
                    )


                    // Receiver
                    .receiverName(
                            request.getReceiverName()
                    )

                    .receiverPhone(
                            request.getReceiverPhone()
                    )

                    .receiverEmail(
                            request.getReceiverEmail()
                    )

                    .receiverAddress(
                            request.getReceiverAddress()
                    )

                    .receiverCoordinates(
                            gson.toJson(receiverCoords)
                    )


                    // Package
                    .packageDescription(
                            request.getPackageDescription()
                    )

                    .packageWeightKg(
                            request.getPackageWeightKg()
                    )

                    .packageLengthCm(
                            request.getPackageLengthCm()
                    )

                    .packageWidthCm(
                            request.getPackageWidthCm()
                    )

                    .packageHeightCm(
                            request.getPackageHeightCm()
                    )

                    .packageQuantity(
                            request.getPackageQuantity()
                    )

                    .declaredValue(
                            request.getDeclaredValue()
                    )

                    .isFragile(
                            request.getIsFragile() != null &&
                                    request.getIsFragile()
                    )

                    .specialInstructions(
                            request.getSpecialInstructions()
                    )


                    // Status
                    .status("PENDING")

                    .priority(
                            request.getPriority()
                    )

                    .estimatedDeliveryDate(
                            estimatedDeliveryDate
                    )

                    .build();


            // ----------------------------------------------------
            // 11. Save shipment
            // ----------------------------------------------------

            Shipment savedShipment =
                    shipmentRepository.save(shipment);


            // ----------------------------------------------------
            // 12. Send confirmation email
            // ----------------------------------------------------

            emailService.sendShipmentCreatedEmail(
                    request.getSenderEmail(),
                    trackingNumber,
                    request.getSenderName(),
                    request.getReceiverName()
            );


            log.info(
                    "Shipment created successfully: {}",
                    trackingNumber
            );

            log.info(
                    "Distance: {} km",
                    distanceKm
            );

            log.info(
                    "Estimated time: {} minutes",
                    estimatedTimeMinutes
            );


            return mapToResponse(savedShipment);


        } catch (Exception e) {

            log.error(
                    "Error creating shipment: {}",
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Failed to create shipment: "
                            + e.getMessage()
            );
        }
    }


    // ============================================================
    // GET SHIPMENT
    // ============================================================

    @Override
    public ShipmentResponse getShipment(
            String trackingNumber
    ) {

        Shipment shipment =
                shipmentRepository
                        .findByTrackingNumber(
                                trackingNumber
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Shipment not found: "
                                                + trackingNumber
                                )
                        );

        return mapToResponse(shipment);
    }


    // ============================================================
    // UPDATE SHIPMENT
    // ============================================================

    @Override
    public ShipmentResponse updateShipment(
            String trackingNumber,
            CreateShipmentRequest request
    ) {

        Shipment shipment =
                shipmentRepository
                        .findByTrackingNumber(
                                trackingNumber
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Shipment not found: "
                                                + trackingNumber
                                )
                        );


        // Update fields

        shipment.setSenderName(
                request.getSenderName()
        );

        shipment.setSenderPhone(
                request.getSenderPhone()
        );

        shipment.setReceiverName(
                request.getReceiverName()
        );

        shipment.setReceiverPhone(
                request.getReceiverPhone()
        );

        shipment.setPackageDescription(
                request.getPackageDescription()
        );


        Shipment updated =
                shipmentRepository.save(shipment);

        return mapToResponse(updated);
    }


    // ============================================================
    // DELETE SHIPMENT
    // ============================================================

    @Override
    public boolean deleteShipment(
            String trackingNumber
    ) {

        Shipment shipment =
                shipmentRepository
                        .findByTrackingNumber(
                                trackingNumber
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Shipment not found"
                                )
                        );

        shipmentRepository.delete(shipment);

        return true;
    }


    // ============================================================
    // GET USER SHIPMENTS
    // ============================================================

    @Override
    public List<ShipmentResponse> getUserShipments(
            Long userId
    ) {

        return shipmentRepository
                .findByCreatedBy_Id(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // ============================================================
    // GET SHIPMENTS BY STATUS
    // ============================================================

    @Override
    public List<ShipmentResponse> getShipmentsByStatus(
            String status
    ) {

        return shipmentRepository
                .findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // ============================================================
    // ADD TRACKING EVENT
    // ============================================================

    @Override
    public TrackingEventResponse addTrackingEvent(
            UpdateShipmentStatusRequest request,
            Long userId
    ) {

        try {

            // ----------------------------------------------------
            // 1. Find shipment
            // ----------------------------------------------------

            Shipment shipment =
                    shipmentRepository
                            .findByTrackingNumber(
                                    request.getTrackingNumber()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Shipment not found"
                                    )
                            );


            // ----------------------------------------------------
            // 2. Find operator
            // ----------------------------------------------------

            User operator =
                    userRepository
                            .findById(userId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Operator not found"
                                    )
                            );


            // ----------------------------------------------------
            // 3. Create tracking event
            // ----------------------------------------------------

            TrackingEvent event =
                    TrackingEvent.builder()

                            .shipment(shipment)

                            .status(
                                    request.getStatus()
                            )

                            .location(
                                    request.getLocation()
                            )

                            .latitude(
                                    request.getLatitude()
                            )

                            .longitude(
                                    request.getLongitude()
                            )

                            .notes(
                                    request.getNotes()
                            )

                            .updatedBy(operator)

                            .build();


            // ----------------------------------------------------
            // 4. Save tracking event
            // ----------------------------------------------------

            TrackingEvent savedEvent =
                    trackingEventRepository.save(event);


            // ----------------------------------------------------
            // 5. Update shipment status
            // ----------------------------------------------------

            shipment.setStatus(
                    request.getStatus()
            );

            shipmentRepository.save(shipment);


            // ----------------------------------------------------
            // 6. Send notification email
            // ----------------------------------------------------

            emailService.sendShipmentStatusEmail(
                    shipment.getReceiverEmail(),
                    shipment.getTrackingNumber(),
                    request.getStatus(),
                    request.getLocation(),
                    LocalDateTime.now().toString()
            );


            log.info(
                    "Tracking event added for shipment: {}",
                    request.getTrackingNumber()
            );


            return mapTrackingEventToResponse(
                    savedEvent
            );


        } catch (Exception e) {

            log.error(
                    "Error adding tracking event: {}",
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Failed to add tracking event: "
                            + e.getMessage()
            );
        }
    }


    // ============================================================
    // GET TRACKING HISTORY
    // ============================================================

    @Override
    public List<TrackingEventResponse> getTrackingHistory(
            String trackingNumber
    ) {

        return trackingEventRepository
                .findByShipment_TrackingNumber(
                        trackingNumber
                )
                .stream()
                .map(this::mapTrackingEventToResponse)
                .collect(Collectors.toList());
    }


    // ============================================================
    // CANCEL SHIPMENT
    // ============================================================

    @Override
    public ShipmentResponse cancelShipment(
            String trackingNumber,
            String reason
    ) {

        Shipment shipment =
                shipmentRepository
                        .findByTrackingNumber(
                                trackingNumber
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Shipment not found"
                                )
                        );


        shipment.setStatus("CANCELLED");

        shipment.setCancellationReason(
                reason
        );


        Shipment updated =
                shipmentRepository.save(shipment);

        return mapToResponse(updated);
    }


    // ============================================================
    // GENERATE TRACKING NUMBER
    // ============================================================

    private String generateTrackingNumber() {

        return "STK"
                + System.currentTimeMillis()
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }


    // ============================================================
    // SHIPMENT -> RESPONSE
    // ============================================================

    private ShipmentResponse mapToResponse(
            Shipment shipment
    ) {

        return ShipmentResponse.builder()

                .id(
                        shipment.getId()
                )

                .trackingNumber(
                        shipment.getTrackingNumber()
                )

                .senderName(
                        shipment.getSenderName()
                )

                .senderPhone(
                        shipment.getSenderPhone()
                )

                .senderEmail(
                        shipment.getSenderEmail()
                )

                .senderAddress(
                        shipment.getSenderAddress()
                )

                .receiverName(
                        shipment.getReceiverName()
                )

                .receiverPhone(
                        shipment.getReceiverPhone()
                )

                .receiverEmail(
                        shipment.getReceiverEmail()
                )

                .receiverAddress(
                        shipment.getReceiverAddress()
                )

                .status(
                        shipment.getStatus()
                )

                .priority(
                        shipment.getPriority()
                )

                .packageDescription(
                        shipment.getPackageDescription()
                )

                .packageWeightKg(
                        shipment.getPackageWeightKg()
                )

                .estimatedDeliveryDate(
                        shipment.getEstimatedDeliveryDate()
                )

                .actualDeliveryDate(
                        shipment.getActualDeliveryDate()
                )

                .createdAt(
                        shipment.getCreatedAt()
                )

                .updatedAt(
                        shipment.getUpdatedAt()
                )

                .build();
    }


    // ============================================================
    // TRACKING EVENT -> RESPONSE
    // ============================================================

    private TrackingEventResponse mapTrackingEventToResponse(
            TrackingEvent event
    ) {

        return TrackingEventResponse.builder()

                .id(
                        event.getId()
                )

                .status(
                        event.getStatus()
                )

                .location(
                        event.getLocation()
                )

                .latitude(
                        event.getLatitude()
                )

                .longitude(
                        event.getLongitude()
                )

                .notes(
                        event.getNotes()
                )

                .eventTimestamp(
                        event.getEventTimestamp()
                )

                .updatedByName(
                        event.getUpdatedBy() != null
                                ? event.getUpdatedBy()
                                .getFullName()
                                : null
                )

                .photoUrl(
                        event.getPhotoUrl()
                )

                .build();
    }
}