package com.shiptrack.shiptrack_pro.service;

public interface ReportService {

    byte[] generateShipmentReport(
            Long userId,
            String role,
            String format
    );

    byte[] generateDeliveryReport(
            Long userId,
            String role,
            String format
    );

    byte[] generateRoutePerformanceReport(
            Long userId,
            String role,
            String format
    );

    byte[] generateDelayAnalysisReport(
            Long userId,
            String role,
            String format
    );
}
