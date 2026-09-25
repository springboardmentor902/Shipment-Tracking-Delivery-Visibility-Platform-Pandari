package com.shiptrack.shiptrack_pro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true)
    private String trackingNumber;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "assigned_operator_id")
    private User assignedOperator;

    // =========================
    // Sender Information
    // =========================

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(name = "sender_phone")
    private String senderPhone;

    @Column(name = "sender_email")
    private String senderEmail;

    @Column(name = "sender_address", nullable = false, columnDefinition = "TEXT")
    private String senderAddress;

    // =========================
    // Receiver Information
    // =========================

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "receiver_phone")
    private String receiverPhone;

    @Column(name = "receiver_email")
    private String receiverEmail;

    @Column(name = "receiver_address", nullable = false, columnDefinition = "TEXT")
    private String receiverAddress;

    // =========================
    // Address Coordinates
    // Stored as PostgreSQL JSONB
    // =========================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sender_coordinates", columnDefinition = "jsonb")
    private String senderCoordinates;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "receiver_coordinates", columnDefinition = "jsonb")
    private String receiverCoordinates;

    // =========================
    // Shipment Details
    // =========================

    @Column(name = "shipment_status")
    private String status;

    @Column(name = "priority")
    private String priority;

    @Column(name = "package_description", columnDefinition = "TEXT")
    private String packageDescription;

    @Column(name = "package_weight_kg")
    private BigDecimal packageWeightKg;

    @Column(name = "package_length_cm")
    private BigDecimal packageLengthCm;

    @Column(name = "package_width_cm")
    private BigDecimal packageWidthCm;

    @Column(name = "package_height_cm")
    private BigDecimal packageHeightCm;

    @Column(name = "package_quantity")
    private Integer packageQuantity;

    @Column(name = "declared_value")
    private BigDecimal declaredValue;

    @Column(name = "is_fragile")
    private Boolean isFragile;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    // =========================
    // Dates
    // =========================

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // =========================
    // Audit
    // =========================

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "business_id")
    private Long businessId;
}