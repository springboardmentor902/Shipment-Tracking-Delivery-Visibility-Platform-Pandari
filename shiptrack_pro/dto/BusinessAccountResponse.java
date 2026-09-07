package com.shiptrack.shiptrack_pro.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAccountResponse {
    private Long id;
    private String companyName;
    private String gstNumber;
    private String businessAddress;
    private String businessPhone;
    private String businessEmail;
    private String industryType;
    private String website;
    private String accountStatus;
    private String verificationStatus;
    private Double shippingRate;
    private Integer monthlyShipmentLimit;
    private LocalDateTime contractStartDate;
    private LocalDateTime contractEndDate;
}
