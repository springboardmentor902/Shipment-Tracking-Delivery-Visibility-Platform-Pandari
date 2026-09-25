package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findByCreatedBy_Id(Long userId);
    List<Shipment> findByAssignedOperator_Id(Long operatorId);
    List<Shipment> findByStatus(String status);
    List<Shipment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT s FROM Shipment s WHERE s.createdBy.id = :userId AND s.status = :status")
    List<Shipment> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
}








