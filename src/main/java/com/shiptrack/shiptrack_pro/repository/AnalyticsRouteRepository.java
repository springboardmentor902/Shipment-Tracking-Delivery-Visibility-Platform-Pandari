package com.shiptrack.shiptrack_pro.repository;

import com.shiptrack.shiptrack_pro.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsRouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByShipment_CreatedBy_Id(Long userId);
    List<Route> findByShipment_BusinessId(Long businessId);
}
