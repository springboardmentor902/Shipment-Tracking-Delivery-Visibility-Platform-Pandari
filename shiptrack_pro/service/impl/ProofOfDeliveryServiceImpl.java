package com.shiptrack.shiptrack_pro.service.impl;


import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryRequest;
import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryResponse;
import com.shiptrack.shiptrack_pro.entity.ProofOfDelivery;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.ProofOfDeliveryRepository;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.FileStorageService;
import com.shiptrack.shiptrack_pro.service.ProofOfDeliveryService;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProofOfDeliveryServiceImpl implements ProofOfDeliveryService {

    private final ProofOfDeliveryRepository proofOfDeliveryRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Override
    public ProofOfDeliveryResponse submitProof(
            Long shipmentId,
            ProofOfDeliveryRequest request,
            MultipartFile signature,
            MultipartFile deliveryPhoto) {

        // 1. Find shipment
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Shipment not found with ID: " + shipmentId));

        // 2. Prevent duplicate POD
        if (proofOfDeliveryRepository
                .findByShipment_Id(shipmentId)
                .isPresent()) {

            throw new RuntimeException(
                    "Proof of delivery already exists for shipment ID: "
                            + shipmentId);
        }

        // 3. Validate tracking number
        if (request.getTrackingNumber() != null &&
                !request.getTrackingNumber()
                        .equals(shipment.getTrackingNumber())) {

            throw new RuntimeException(
                    "Tracking number does not match shipment");
        }

        // 4. Store signature
        String signatureUrl = null;

        if (signature != null && !signature.isEmpty()) {

            signatureUrl = fileStorageService.storeFile(
                    signature,
                    "signature"
            );
        }

        // 5. Store delivery photo
        String deliveryPhotoUrl = null;

        if (deliveryPhoto != null && !deliveryPhoto.isEmpty()) {

            deliveryPhotoUrl = fileStorageService.storeFile(
                    deliveryPhoto,
                    "photo"
            );
        }

        // 6. Create POD
        ProofOfDelivery pod = ProofOfDelivery.builder()
                .shipment(shipment)
                .signatureUrl(signatureUrl)
                .deliveryPhotoUrl(deliveryPhotoUrl)
                .deliveredToName(request.getDeliveredToName())
                .recipientPhone(request.getRecipientPhone())
                .deliveryNotes(request.getDeliveryNotes())
                .verificationStatus("PENDING")
                .deliveryLatitude(request.getDeliveryLatitude())
                .deliveryLongitude(request.getDeliveryLongitude())
                .deliveryAddress(request.getDeliveryAddress())
                .build();

        ProofOfDelivery savedPod =
                proofOfDeliveryRepository.save(pod);

        // 7. Update shipment
        shipment.setStatus("DELIVERED");
        shipment.setActualDeliveryDate(LocalDateTime.now());

        shipmentRepository.save(shipment);

        log.info(
                "Proof of Delivery submitted successfully for shipment {}",
                shipmentId
        );

        // 8. Return response
        return convertToResponse(savedPod);
    }

    @Override
    public ProofOfDeliveryResponse verifyProof(
            Long shipmentId,
            String verificationStatus,
            String rejectionReason,
            Long verifiedByUserId) {

        // 1. Find POD
        ProofOfDelivery pod =
                proofOfDeliveryRepository
                        .findByShipment_Id(shipmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Proof of delivery not found for shipment ID: "
                                                + shipmentId));

        // 2. Validate status
        if (!"APPROVED".equalsIgnoreCase(verificationStatus)
                && !"REJECTED".equalsIgnoreCase(verificationStatus)) {

            throw new IllegalArgumentException(
                    "Verification status must be APPROVED or REJECTED");
        }

        // 3. Find verifying user
        User verifiedBy =
                userRepository.findById(verifiedByUserId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found with ID: "
                                                + verifiedByUserId));

        // 4. Update verification
        pod.setVerificationStatus(
                verificationStatus.toUpperCase());

        pod.setVerifiedBy(verifiedBy);

        pod.setVerifiedAt(LocalDateTime.now());

        if ("REJECTED".equalsIgnoreCase(verificationStatus)) {

            pod.setRejectionReason(rejectionReason);

        } else {

            pod.setRejectionReason(null);
        }

        ProofOfDelivery updatedPod =
                proofOfDeliveryRepository.save(pod);

        log.info(
                "POD for shipment {} verified as {} by user {}",
                shipmentId,
                verificationStatus,
                verifiedByUserId
        );

        return convertToResponse(updatedPod);
    }

    @Override
    @Transactional(readOnly = true)
    public ProofOfDeliveryResponse getProof(Long shipmentId) {

        ProofOfDelivery pod =
                proofOfDeliveryRepository
                        .findByShipment_Id(shipmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Proof of delivery not found for shipment ID: "
                                                + shipmentId));

        return convertToResponse(pod);
    }

    // ----------------------------------------------------
    // ENTITY → RESPONSE DTO
    // ----------------------------------------------------

    private ProofOfDeliveryResponse convertToResponse(
            ProofOfDelivery pod) {

        Shipment shipment = pod.getShipment();

        return ProofOfDeliveryResponse.builder()
                .id(pod.getId())
                .trackingNumber(
                        shipment != null
                                ? shipment.getTrackingNumber()
                                : null)
                .signatureUrl(pod.getSignatureUrl())
                .deliveryPhotoUrl(pod.getDeliveryPhotoUrl())
                .deliveredToName(pod.getDeliveredToName())
                .deliveryNotes(pod.getDeliveryNotes())
                .verificationStatus(
                        pod.getVerificationStatus())
                .deliveredAt(pod.getDeliveredAt())
                .verifiedAt(pod.getVerifiedAt())
                .build();
    }
}
