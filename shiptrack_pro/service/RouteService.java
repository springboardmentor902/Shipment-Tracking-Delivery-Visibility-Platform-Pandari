package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.CreateRouteRequest;
import com.shiptrack.shiptrack_pro.dto.LocationUpdateRequest;
import com.shiptrack.shiptrack_pro.dto.LocationUpdateResponse;
import com.shiptrack.shiptrack_pro.dto.RouteResponse;

public interface RouteService {

    RouteResponse createRoute(
            CreateRouteRequest request
    );

    RouteResponse updateRoute(
            Long routeId,
            CreateRouteRequest request
    );

    RouteResponse getRouteByShipment(
            Long shipmentId
    );

    LocationUpdateResponse updateLocation(
            Long routeId,
            LocationUpdateRequest request
    );
}
