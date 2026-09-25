package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.ReportService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    // =========================================================
    // SHIPMENT REPORT
    // =========================================================

    @GetMapping("/shipments")
    public ResponseEntity<byte[]> shipmentReport(
            @RequestParam(defaultValue = "pdf") String format,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        validateFormat(format);

        byte[] file =
                reportService.generateShipmentReport(
                        user.getId(),
                        user.getRole(),
                        format
                );

        return buildResponse(
                file,
                format,
                "shipment-report"
        );
    }

    // =========================================================
    // DELIVERY REPORT
    // =========================================================

    @GetMapping("/deliveries")
    public ResponseEntity<byte[]> deliveryReport(
            @RequestParam(defaultValue = "pdf") String format,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        validateFormat(format);

        byte[] file =
                reportService.generateDeliveryReport(
                        user.getId(),
                        user.getRole(),
                        format
                );

        return buildResponse(
                file,
                format,
                "delivery-report"
        );
    }

    // =========================================================
    // ROUTE PERFORMANCE REPORT
    // =========================================================

    @GetMapping("/routes")
    public ResponseEntity<byte[]> routePerformanceReport(
            @RequestParam(defaultValue = "pdf") String format,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        validateFormat(format);

        byte[] file =
                reportService.generateRoutePerformanceReport(
                        user.getId(),
                        user.getRole(),
                        format
                );

        return buildResponse(
                file,
                format,
                "route-performance-report"
        );
    }

    // =========================================================
    // DELAY ANALYSIS REPORT
    // =========================================================

    @GetMapping("/delays")
    public ResponseEntity<byte[]> delayAnalysisReport(
            @RequestParam(defaultValue = "pdf") String format,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        validateFormat(format);

        byte[] file =
                reportService.generateDelayAnalysisReport(
                        user.getId(),
                        user.getRole(),
                        format
                );

        return buildResponse(
                file,
                format,
                "delay-analysis-report"
        );
    }

    // =========================================================
    // AUTHENTICATED USER
    // =========================================================

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(
                        () -> new RuntimeException(
                                "Authenticated user not found"
                        )
                );
    }

    // =========================================================
    // FORMAT VALIDATION
    // =========================================================

    private void validateFormat(String format) {

        if (!"pdf".equalsIgnoreCase(format)
                && !"excel".equalsIgnoreCase(format)) {

            throw new IllegalArgumentException(
                    "Format must be pdf or excel"
            );
        }
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    private ResponseEntity<byte[]> buildResponse(
            byte[] file,
            String format,
            String fileName
    ) {

        boolean pdf =
                "pdf".equalsIgnoreCase(format);

        MediaType contentType =
                pdf
                        ? MediaType.APPLICATION_PDF
                        : MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                );

        String extension =
                pdf ? ".pdf" : ".xlsx";

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(contentType);

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(fileName + extension)
                        .build()
        );

        headers.setContentLength(file.length);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(file);
    }
}
