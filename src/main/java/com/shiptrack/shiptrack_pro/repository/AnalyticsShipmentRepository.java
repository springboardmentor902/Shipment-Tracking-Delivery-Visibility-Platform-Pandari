package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findByCreatedBy_Id(Long userId);
    List<Shipment> findByBusinessId(Long businessId);
}
