package com.shiptrack.shiptrack_pro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "proof_of_delivery")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProofOfDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "shipment_id", nullable = false, unique = true)
    private Shipment shipment;

    @Column(name = "signature_url", columnDefinition = "TEXT")
    private String signatureUrl;

    @Column(name = "delivery_photo_url", columnDefinition = "TEXT")
    private String deliveryPhotoUrl;

    @Column(name = "delivered_to_name")
    private String deliveredToName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    @Column(name = "verification_status")
    private String verificationStatus; // PENDING, APPROVED, REJECTED

    @ManyToOne
    @JoinColumn(name = "verified_by_user_id")
    private User verifiedBy;

    @CreationTimestamp
    @Column(name = "delivered_at", updatable = false)
    private LocalDateTime deliveredAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "delivery_latitude")
    private Double deliveryLatitude;

    @Column(name = "delivery_longitude")
    private Double deliveryLongitude;

    @Column(name = "delivery_address")
    private String deliveryAddress;
}

