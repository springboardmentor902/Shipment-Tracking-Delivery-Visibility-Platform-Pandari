package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryRequest;
import com.shiptrack.shiptrack_pro.dto.ProofOfDeliveryResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProofOfDeliveryService {

    ProofOfDeliveryResponse submitProof(
            Long shipmentId,
            ProofOfDeliveryRequest request,
            MultipartFile signature,
            MultipartFile deliveryPhoto
    );

    ProofOfDeliveryResponse verifyProof(
            Long shipmentId,
            String verificationStatus,
            String rejectionReason,
            Long verifiedByUserId
    );

    ProofOfDeliveryResponse getProof(Long shipmentId);
}
