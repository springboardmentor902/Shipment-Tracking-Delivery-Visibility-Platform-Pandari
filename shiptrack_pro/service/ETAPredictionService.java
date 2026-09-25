package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.ETAPredictionResponse;

import java.util.List;

public interface ETAPredictionService {

    // Calculate/recalculate ETA for a shipment
    ETAPredictionResponse predictETA(Long shipmentId);

    // Get the existing ETA prediction
    ETAPredictionResponse getPrediction(Long shipmentId);

    // Get shipments whose delay risk is above the threshold
    List<ETAPredictionResponse> getAtRiskShipments();
}