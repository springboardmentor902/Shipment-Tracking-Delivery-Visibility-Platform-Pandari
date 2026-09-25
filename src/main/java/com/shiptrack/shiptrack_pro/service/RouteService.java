package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.CreateRouteRequest;
import com.shiptrack.shiptrack_pro.dto.LocationUpdateRequest;
import com.shiptrack.shiptrack_pro.dto.LocationUpdateResponse;
import com.shiptrack.shiptrack_pro.dto.RouteResponse;

import java.util.List;

public interface RouteService {

    RouteResponse createRoute(
            CreateRouteRequest request
    );

    RouteResponse updateRoute(
            Long routeId,
            CreateRouteRequest request
    );

    // Returns only the current route
    RouteResponse getRouteByShipment(
            Long shipmentId
    );

    // Returns complete route history
    List<RouteResponse> getRouteHistory(
            Long shipmentId
    );

    LocationUpdateResponse updateLocation(
            Long routeId,
            LocationUpdateRequest request
    );
}