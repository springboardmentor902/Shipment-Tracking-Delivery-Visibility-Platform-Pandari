package com.shiptrack.shiptrack_pro.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRouteRequest {
    private Long driverId;
    private List<Long> shipmentIds;
    private String origin;
    private String destination;
}
