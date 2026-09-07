package com.shiptrack.shiptrack_pro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProofVerificationRequest {

    @NotBlank(message = "Verification status is required")
    private String verificationStatus;

    private String rejectionReason;
}