package com.shiptrack.shiptrack_pro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryRequest;
import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryResponse;
import com.shiptrack.shiptrack_pro.dto.ProofVerificationRequest;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.ProofOfDeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pod")
@RequiredArgsConstructor
public class ProofOfDeliveryController {

    private final ProofOfDeliveryService proofOfDeliveryService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;


    // ==========================================================
    // SUBMIT PROOF OF DELIVERY
    // ==========================================================

    @PostMapping(
            value = "/{shipmentId}",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ProofOfDeliveryResponse> submitProof(

            @PathVariable Long shipmentId,

            @RequestPart("data")
            String data,

            @RequestPart(
                    value = "signature",
                    required = false
            )
            MultipartFile signature,

            @RequestPart(
                    value = "deliveryPhoto",
                    required = false
            )
            MultipartFile deliveryPhoto
    ) {

        try {

            // Convert JSON string into DTO
            ProofOfDeliveryRequest request =
                    objectMapper.readValue(
                            data,
                            ProofOfDeliveryRequest.class
                    );

            // Validate required fields manually
            if (request.getTrackingNumber() == null ||
                    request.getTrackingNumber().isBlank()) {

                throw new IllegalArgumentException(
                        "Tracking number is required"
                );
            }

            if (request.getDeliveredToName() == null ||
                    request.getDeliveredToName().isBlank()) {

                throw new IllegalArgumentException(
                        "Delivered to name is required"
                );
            }

            // Call service
            ProofOfDeliveryResponse response =
                    proofOfDeliveryService.submitProof(
                            shipmentId,
                            request,
                            signature,
                            deliveryPhoto
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid POD data: " + e.getMessage(),
                    e
            );
        }
    }


    // ==========================================================
    // VERIFY PROOF OF DELIVERY
    // ==========================================================

    @PatchMapping("/{shipmentId}/verify")
    public ResponseEntity<ProofOfDeliveryResponse> verifyProof(

            @PathVariable Long shipmentId,

            @Valid @RequestBody
            ProofVerificationRequest request,

            org.springframework.security.core.Authentication authentication
    ) {

        User verifiedBy =
                getAuthenticatedUser(authentication);

        ProofOfDeliveryResponse response =
                proofOfDeliveryService.verifyProof(
                        shipmentId,
                        request.getVerificationStatus(),
                        request.getRejectionReason(),
                        verifiedBy.getId()
                );

        return ResponseEntity.ok(response);
    }


    // ==========================================================
    // GET POD
    // ==========================================================

    @GetMapping("/{shipmentId}")
    public ResponseEntity<ProofOfDeliveryResponse> getProof(

            @PathVariable Long shipmentId
    ) {

        ProofOfDeliveryResponse response =
                proofOfDeliveryService.getProof(shipmentId);

        return ResponseEntity.ok(response);
    }


    // ==========================================================
    // GET AUTHENTICATED USER
    // ==========================================================

    private User getAuthenticatedUser(
            org.springframework.security.core.Authentication authentication
    ) {

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );
    }
}