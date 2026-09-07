package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.CreateShipmentRequest;
import com.shiptrack.shiptrack_pro.dto.ShipmentResponse;
import com.shiptrack.shiptrack_pro.dto.UpdateShipmentStatusRequest;
import com.shiptrack.shiptrack_pro.dto.TrackingEventResponse;
import com.shiptrack.shiptrack_pro.entity.Shipment;

import java.util.List;
import java.util.Optional;

public interface ShipmentService {
    ShipmentResponse createShipment(CreateShipmentRequest request, Long userId);
    ShipmentResponse getShipment(String trackingNumber);
    ShipmentResponse updateShipment(String trackingNumber, CreateShipmentRequest request);
    boolean deleteShipment(String trackingNumber);
    List<ShipmentResponse> getUserShipments(Long userId);
    List<ShipmentResponse> getShipmentsByStatus(String status);
    TrackingEventResponse addTrackingEvent(UpdateShipmentStatusRequest request, Long userId);
    List<TrackingEventResponse> getTrackingHistory(String trackingNumber);
    ShipmentResponse cancelShipment(String trackingNumber, String reason);
}
