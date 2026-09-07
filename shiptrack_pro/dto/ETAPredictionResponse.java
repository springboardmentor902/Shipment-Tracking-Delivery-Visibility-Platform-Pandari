package com.shiptrack.shiptrack_pro.dto;

import java.time.LocalDateTime;

public class ETAPredictionResponse {

    private Long shipmentId;
    private LocalDateTime predictedDeliveryTime;
    private Double delayRiskScore;
    private Double confidenceScore;
    private String factors;
    private LocalDateTime calculatedAt;

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public LocalDateTime getPredictedDeliveryTime() {
        return predictedDeliveryTime;
    }

    public void setPredictedDeliveryTime(LocalDateTime predictedDeliveryTime) {
        this.predictedDeliveryTime = predictedDeliveryTime;
    }

    public Double getDelayRiskScore() {
        return delayRiskScore;
    }

    public void setDelayRiskScore(Double delayRiskScore) {
        this.delayRiskScore = delayRiskScore;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getFactors() {
        return factors;
    }

    public void setFactors(String factors) {
        this.factors = factors;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}
