package com.shiptrack.shiptrack_pro.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEventResponse {
    private Long id;
    private String status;
    private String location;
    private Double latitude;
    private Double longitude;
    private String notes;
    private LocalDateTime eventTimestamp;
    private String updatedByName;
    private String photoUrl;
}
