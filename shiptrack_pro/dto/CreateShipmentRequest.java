package com.shiptrack.shiptrack_pro.dto;

import lombok.*;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentRequest {

    @NotBlank(message = "Sender name is required")
    private String senderName;

    @NotBlank(message = "Sender phone is required")
    private String senderPhone;

    private String senderEmail;

    @NotBlank(message = "Sender address is required")
    private String senderAddress;

    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Receiver phone is required")
    private String receiverPhone;

    private String receiverEmail;

    @NotBlank(message = "Receiver address is required")
    private String receiverAddress;

    @NotBlank(message = "Package description is required")
    private String packageDescription;

    @NotNull(message = "Package weight is required")
    @DecimalMin(value = "0.1")
    private BigDecimal packageWeightKg;

    private BigDecimal packageLengthCm;
    private BigDecimal packageWidthCm;
    private BigDecimal packageHeightCm;

    @NotNull(message = "Package quantity is required")
    private Integer packageQuantity;

    private BigDecimal declaredValue;
    private Boolean isFragile;
    private String specialInstructions;

    @NotBlank(message = "Priority is required")
    @Pattern(regexp = "STANDARD|EXPRESS|OVERNIGHT", message = "Priority must be STANDARD, EXPRESS, or OVERNIGHT")
    private String priority;
}
