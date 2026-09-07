package com.shiptrack.shiptrack_pro.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAccountRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String gstNumber;

    @NotBlank(message = "Business address is required")
    private String businessAddress;

    private String businessPhone;
    private String businessEmail;
    private String industryType;
    private String website;
}
