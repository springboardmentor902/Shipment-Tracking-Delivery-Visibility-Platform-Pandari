package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    // Existing queries
    List<Route> findByDriver_Id(Long driverId);

    List<Route> findByRouteStatus(String routeStatus);

    Optional<Route> findTopByShipment_IdOrderByCreatedAtDesc(Long shipmentId);

    @Query("""
            SELECT r
            FROM Route r
            WHERE r.driver.id = :driverId
            AND r.routeStatus = :status
            """)
    List<Route> findActiveRoutesByDriver(
            @Param("driverId") Long driverId,
            @Param("status") String status
    );

    List<Route> findByShipment_CreatedBy_Id(Long userId);

    List<Route> findByShipment_BusinessId(Long businessId);


    // =========================================================
    // ROUTE HISTORY
    // =========================================================

    List<Route> findByShipment_IdOrderByCreatedAtAsc(Long shipmentId);


    // =========================================================
    // CURRENT ROUTE
    // =========================================================

    Optional<Route> findByShipment_IdAndIsCurrentTrue(Long shipmentId);


    // =========================================================
    // FIND CURRENT ROUTE(S) FOR DEACTIVATION
    // =========================================================

    List<Route> findAllByShipment_IdAndIsCurrentTrue(Long shipmentId);
}