package com.shiptrack.shiptrack_pro.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eta_predictions")
public class ETAPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false, unique = true)
    private Shipment shipment;

    @Column(name = "predicted_delivery_time", nullable = false)
    private LocalDateTime predictedDeliveryTime;

    @Column(name = "delay_risk_score", nullable = false)
    private Double delayRiskScore;

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;

    @Column(name = "factors", columnDefinition = "TEXT")
    private String factors;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    public ETAPrediction() {
    }

    public Long getId() {
        return id;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
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
