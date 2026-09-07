package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.TrackingEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingEventRepository
        extends JpaRepository<TrackingEvent, Long> {

    List<TrackingEvent> findByShipment_Id(Long shipmentId);

    List<TrackingEvent> findByShipment_TrackingNumber(
            String trackingNumber
    );

    List<TrackingEvent> findByStatus(String status);

    List<TrackingEvent> findByUpdatedBy_Id(Long userId);

    @Query("""
        SELECT t
        FROM TrackingEvent t
        WHERE t.shipment.id = :shipmentId
        ORDER BY t.eventTimestamp DESC
    """)
    List<TrackingEvent> getShipmentTrackingHistory(
            @Param("shipmentId") Long shipmentId
    );

    @Query("""
        SELECT t
        FROM TrackingEvent t
        WHERE t.shipment.id = :shipmentId
        ORDER BY t.eventTimestamp DESC
    """)
    List<TrackingEvent> findRecentTrackingEvents(
            @Param("shipmentId") Long shipmentId,
            Pageable pageable
    );

    List<TrackingEvent> findTop10ByShipmentIdOrderByEventTimestampDesc(Long shipmentId);
}
