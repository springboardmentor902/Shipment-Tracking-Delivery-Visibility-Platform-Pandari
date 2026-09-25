package com.shiptrack.shiptrack_pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteAlternativeDTO {

    /**
     * Route distance in kilometers.
     */
    private Double distanceKm;

    /**
     * Normal estimated duration in minutes.
     */
    private Integer durationMinutes;

    /**
     * Duration adjusted for traffic in minutes.
     */
    private Integer trafficAdjustedDurationMinutes;

    /**
     * Human-readable route description.
     */
    private String routeSummary;

    /**
     * Indicates whether this route was selected
     * as the optimized route.
     */
    private Boolean selected;

    /**
     * Explanation for why this route was selected.
     */
    private String selectionReason;
}
