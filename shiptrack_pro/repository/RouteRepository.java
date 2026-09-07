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

    List<Route> findByDriver_Id(Long driverId);

    List<Route> findByRouteStatus(String routeStatus);

    // Get the latest route created for a shipment
    Optional<Route> findTopByShipment_IdOrderByCreatedAtDesc(Long shipmentId);

    @Query("SELECT r FROM Route r WHERE r.driver.id = :driverId AND r.routeStatus = :status")
    List<Route> findActiveRoutesByDriver(
            @Param("driverId") Long driverId,
            @Param("status") String status
    );
}
