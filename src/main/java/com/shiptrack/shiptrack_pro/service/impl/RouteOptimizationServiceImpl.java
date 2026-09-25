package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.RouteAlternativeDTO;
import com.shiptrack.shiptrack_pro.service.RouteOptimizationService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class RouteOptimizationServiceImpl implements RouteOptimizationService {

    @Override
    public RouteAlternativeDTO selectBestRoute(
            List<RouteAlternativeDTO> alternatives) {

        if (alternatives == null || alternatives.isEmpty()) {
            throw new IllegalArgumentException(
                    "No route alternatives available"
            );
        }

        RouteAlternativeDTO bestRoute = alternatives.stream()
                .filter(route ->
                        route.getTrafficAdjustedDurationMinutes() != null)
                .min(Comparator.comparing(
                        RouteAlternativeDTO::getTrafficAdjustedDurationMinutes
                ))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No route has a valid traffic-adjusted duration"
                        ));

        // Reset selection information
        alternatives.forEach(route -> {
            route.setSelected(false);
            route.setSelectionReason(null);
        });

        bestRoute.setSelected(true);

        bestRoute.setSelectionReason(
                "Selected because it has the lowest "
                        + "traffic-adjusted travel time of "
                        + bestRoute.getTrafficAdjustedDurationMinutes()
                        + " minutes."
        );

        return bestRoute;
    }
}
