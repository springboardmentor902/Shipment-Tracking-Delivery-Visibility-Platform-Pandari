package com.shiptrack.shiptrack_pro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "gst_number", unique = true)
    private String gstNumber;

    @Column(name = "business_address", columnDefinition = "TEXT")
    private String businessAddress;

    @Column(name = "business_phone")
    private String businessPhone;

    @Column(name = "business_email", unique = true)
    private String businessEmail;

    @Column(name = "industry_type")
    private String industryType;

    @Column(name = "website")
    private String website;

    @Column(name = "account_status")
    private String accountStatus; // ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION

    @Column(name = "verification_status")
    private String verificationStatus; // VERIFIED, PENDING, REJECTED

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "shipping_rate")
    private Double shippingRate;

    @Column(name = "monthly_shipment_limit")
    private Integer monthlyShipmentLimit;

    @Column(name = "current_shipment_count")
    private Integer currentShipmentCount;

    @Column(name = "contract_start_date")
    private LocalDateTime contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDateTime contractEndDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
}

