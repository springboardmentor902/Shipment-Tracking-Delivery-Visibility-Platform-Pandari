package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.ETAPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ETAPredictionRepository
        extends JpaRepository<ETAPrediction, Long> {

    Optional<ETAPrediction> findByShipment_Id(Long shipmentId);

    List<ETAPrediction> findByDelayRiskScoreGreaterThan(
            Double riskScore
    );
}
