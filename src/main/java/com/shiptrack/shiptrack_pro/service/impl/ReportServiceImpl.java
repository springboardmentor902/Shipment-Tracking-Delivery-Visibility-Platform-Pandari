package com.shiptrack.shiptrack_pro.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.Cell;
import com.lowagie.text.pdf.PdfWriter;

import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.RouteRepository;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.ReportService;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ShipmentRepository shipmentRepository;
    private final RouteRepository routeRepository;
    private final UserRepository userRepository;

    // =========================================================
    // SHIPMENT REPORT
    // =========================================================

    @Override
    public byte[] generateShipmentReport(
            Long userId,
            String role,
            String format
    ) {

        List<Shipment> shipments = getShipmentsForUser(userId, role);

        if ("pdf".equalsIgnoreCase(format)) {
            return generateShipmentPdf(shipments);
        }

        return generateShipmentExcel(shipments);
    }

    // =========================================================
    // DELIVERY REPORT
    // =========================================================

    @Override
    public byte[] generateDeliveryReport(
            Long userId,
            String role,
            String format
    ) {

        List<Shipment> shipments = getShipmentsForUser(userId, role);

        if ("pdf".equalsIgnoreCase(format)) {
            return generateDeliveryPdf(shipments);
        }

        return generateDeliveryExcel(shipments);
    }

    // =========================================================
    // ROUTE PERFORMANCE REPORT
    // =========================================================

    @Override
    public byte[] generateRoutePerformanceReport(
            Long userId,
            String role,
            String format
    ) {

        List<Route> routes = getRoutesForUser(userId, role);

        if ("pdf".equalsIgnoreCase(format)) {
            return generateRoutePdf(routes);
        }

        return generateRouteExcel(routes);
    }

    // =========================================================
    // DELAY ANALYSIS REPORT
    // =========================================================

    @Override
    public byte[] generateDelayAnalysisReport(
            Long userId,
            String role,
            String format
    ) {

        List<Shipment> shipments = getShipmentsForUser(userId, role);

        if ("pdf".equalsIgnoreCase(format)) {
            return generateDelayPdf(shipments);
        }

        return generateDelayExcel(shipments);
    }

    // =========================================================
    // OWNERSHIP RESTRICTION
    // =========================================================

    private List<Shipment> getShipmentsForUser(
            Long userId,
            String role
    ) {

        if ("ADMINISTRATOR".equals(role)) {
            return shipmentRepository.findAll();
        }

        if ("BUSINESS_CLIENT".equals(role)) {
            return shipmentRepository.findByBusinessId(userId);
        }

        return shipmentRepository.findByCreatedBy_Id(userId);
    }

    private List<Route> getRoutesForUser(
            Long userId,
            String role
    ) {

        if ("ADMINISTRATOR".equals(role)) {
            return routeRepository.findAll();
        }

        if ("BUSINESS_CLIENT".equals(role)) {
            return routeRepository.findByShipment_BusinessId(userId);
        }

        return routeRepository.findByShipment_CreatedBy_Id(userId);
    }

    // =========================================================
    // SHIPMENT PDF
    // =========================================================

    private byte[] generateShipmentPdf(
            List<Shipment> shipments
    ) {

        try {

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            Document document = new Document();

            PdfWriter.getInstance(document, output);

            document.open();

            document.add(
                    new Paragraph("ShipTrack - Shipment Report")
            );

            document.add(
                    new Paragraph(
                            "Generated: " + LocalDateTime.now()
                    )
            );

            document.add(new Paragraph(" "));

            Table table = new Table(6);

            table.addCell(new Cell("Tracking Number"));
            table.addCell(new Cell("Sender"));
            table.addCell(new Cell("Receiver"));
            table.addCell(new Cell("Status"));
            table.addCell(new Cell("Created Date"));
            table.addCell(new Cell("Estimated Delivery"));

            for (Shipment shipment : shipments) {

                table.addCell(
                        new Cell(safe(shipment.getTrackingNumber()))
                );

                table.addCell(
                        new Cell(safe(shipment.getSenderName()))
                );

                table.addCell(
                        new Cell(safe(shipment.getReceiverName()))
                );

                table.addCell(
                        new Cell(safe(shipment.getStatus()))
                );

                table.addCell(
                        new Cell(
                                shipment.getCreatedAt() == null
                                        ? ""
                                        : shipment.getCreatedAt().toString()
                        )
                );

                table.addCell(
                        new Cell(
                                shipment.getEstimatedDeliveryDate() == null
                                        ? ""
                                        : shipment
                                        .getEstimatedDeliveryDate()
                                        .toString()
                        )
                );
            }

            document.add(table);

            document.close();

            return output.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate shipment PDF",
                    e
            );
        }
    }

    // =========================================================
    // SHIPMENT EXCEL
    // =========================================================

    private byte[] generateShipmentExcel(
            List<Shipment> shipments
    ) {

        try {

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet =
                    workbook.createSheet("Shipments");

            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("Tracking Number");
            header.createCell(1).setCellValue("Sender");
            header.createCell(2).setCellValue("Receiver");
            header.createCell(3).setCellValue("Status");
            header.createCell(4).setCellValue("Created Date");
            header.createCell(5).setCellValue("Estimated Delivery");

            int rowNumber = 1;

            for (Shipment shipment : shipments) {

                Row row =
                        sheet.createRow(rowNumber++);

                row.createCell(0).setCellValue(
                        safe(shipment.getTrackingNumber())
                );

                row.createCell(1).setCellValue(
                        safe(shipment.getSenderName())
                );

                row.createCell(2).setCellValue(
                        safe(shipment.getReceiverName())
                );

                row.createCell(3).setCellValue(
                        safe(shipment.getStatus())
                );

                row.createCell(4).setCellValue(
                        shipment.getCreatedAt() == null
                                ? ""
                                : shipment.getCreatedAt().toString()
                );

                row.createCell(5).setCellValue(
                        shipment.getEstimatedDeliveryDate() == null
                                ? ""
                                : shipment.getEstimatedDeliveryDate().toString()
                );
            }

            autoSize(sheet, 6);

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            workbook.write(output);
            workbook.close();

            return output.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate shipment Excel",
                    e
            );
        }
    }

    // =========================================================
    // DELIVERY PDF
    // =========================================================

    private byte[] generateDeliveryPdf(
            List<Shipment> shipments
    ) {

        try {

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            Document document = new Document();

            PdfWriter.getInstance(document, output);

            document.open();

            document.add(
                    new Paragraph("ShipTrack - Delivery Report")
            );

            Table table = new Table(4);

            table.addCell(new Cell("Tracking Number"));
            table.addCell(new Cell("Receiver"));
            table.addCell(new Cell("Actual Delivery"));
            table.addCell(new Cell("Delivery Status"));

            for (Shipment shipment : shipments) {

                if (!"DELIVERED".equalsIgnoreCase(
                        shipment.getStatus())) {
                    continue;
                }

                table.addCell(
                        new Cell(safe(shipment.getTrackingNumber()))
                );

                table.addCell(
                        new Cell(safe(shipment.getReceiverName()))
                );

                table.addCell(
                        new Cell(
                                shipment.getActualDeliveryDate() == null
                                        ? ""
                                        : shipment
                                        .getActualDeliveryDate()
                                        .toString()
                        )
                );

                table.addCell(
                        new Cell("DELIVERED")
                );
            }

            document.add(table);

            document.close();

            return output.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate delivery PDF",
                    e
            );
        }
    }

    // =========================================================
    // DELIVERY EXCEL
    // =========================================================

    private byte[] generateDeliveryExcel(
            List<Shipment> shipments
    ) {

        try {

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet =
                    workbook.createSheet("Deliveries");

            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("Tracking Number");
            header.createCell(1).setCellValue("Receiver");
            header.createCell(2).setCellValue("Actual Delivery");
            header.createCell(3).setCellValue("Delivery Status");

            int rowNumber = 1;

            for (Shipment shipment : shipments) {

                if (!"DELIVERED".equalsIgnoreCase(
                        shipment.getStatus())) {
                    continue;
                }

                Row row =
                        sheet.createRow(rowNumber++);

                row.createCell(0).setCellValue(
                        safe(shipment.getTrackingNumber())
                );

                row.createCell(1).setCellValue(
                        safe(shipment.getReceiverName())
                );

                row.createCell(2).setCellValue(
                        shipment.getActualDeliveryDate() == null
                                ? ""
                                : shipment
                                .getActualDeliveryDate()
                                .toString()
                );

                row.createCell(3).setCellValue(
                        "DELIVERED"
                );
            }

            autoSize(sheet, 4);

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            workbook.write(output);
            workbook.close();

            return output.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate delivery Excel",
                    e
            );
        }
    }

    // =========================================================
    // ROUTE PDF
    // =========================================================

    private byte[] generateRoutePdf(
            List<Route> routes
    ) {

        try {

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            Document document = new Document();

            PdfWriter.getInstance(document, output);

            document.open();

            document.add(
                    new Paragraph(
                            "ShipTrack - Route Performance Report"
                    )
            );

            Table table = new Table(5);

            table.addCell(new Cell("Route ID"));
            table.addCell(new Cell("Origin"));
            table.addCell(new Cell("Destination"));
            table.addCell(new Cell("Distance KM"));
            table.addCell(new Cell("Estimated Time"));

            for (Route route : routes) {

                table.addCell(
                        new Cell(String.valueOf(route.getId()))
                );

                table.addCell(
                        new Cell(safe(route.getOrigin()))
                );

                table.addCell(
                        new Cell(safe(route.getDestination()))
                );

                table.addCell(
                        new Cell(
                                route.getDistanceKm() == null
                                        ? ""
                                        : route.getDistanceKm().toString()
                        )
                );

                table.addCell(
                        new Cell(
                                route.getEstimatedTimeMinutes() == null
                                        ? ""
                                        : route
                                        .getEstimatedTimeMinutes()
                                        .toString()
                        )
                );
            }

            document.add(table);

            document.close();

            return output.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate route PDF",
                    e
            );
        }
    }

    // =========================================================
    // ROUTE EXCEL
    // =========================================================

    private byte[] generateRouteExcel(
            List<Route> routes
    ) {

        try {

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet =
                    workbook.createSheet("Routes");

            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("Route ID");
            header.createCell(1).setCellValue("Origin");
            header.createCell(2).setCellValue("Destination");
            header.createCell(3).setCellValue("Distance KM");
            header.createCell(4).setCellValue("Estimated Time");
            header.createCell(5).setCellValue("Actual Time");
            header.createCell(6).setCellValue("Traffic");

            int rowNumber = 1;

            for (Route route : routes) {

                Row row =
                        sheet.createRow(rowNumber++);

                row.createCell(0).setCellValue(
                        route.getId()
                );

                row.createCell(1).setCellValue(
                        safe(route.getOrigin())
                );

                row.createCell(2).setCellValue(
                        safe(route.getDestination())
                );

                row.createCell(3).setCellValue(
                        route.getDistanceKm() == null
                                ? 0
                                : route.getDistanceKm()
                );

                row.createCell(4).setCellValue(
                        route.getEstimatedTimeMinutes() == null
                                ? 0
                                : route.getEstimatedTimeMinutes()
                );

                row.createCell(5).setCellValue(
                        route.getActualTimeMinutes() == null
                                ? 0
                                : route.getActualTimeMinutes()
                );

                row.createCell(6).setCellValue(
                        safe(route.getTrafficCondition())
                );
            }

            autoSize(sheet, 7);

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            workbook.write(output);
            workbook.close();

            return output.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate route Excel",
                    e
            );
        }
    }

    // =========================================================
    // DELAY PDF
    // =========================================================

    private byte[] generateDelayPdf(
            List<Shipment> shipments
    ) {

        try {

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            Document document = new Document();

            PdfWriter.getInstance(document, output);

            document.open();

            document.add(
                    new Paragraph(
                            "ShipTrack - Delay Analysis Report"
                    )
            );

            Table table = new Table(5);

            table.addCell(new Cell("Tracking Number"));
            table.addCell(new Cell("Status"));
            table.addCell(new Cell("Estimated Delivery"));
            table.addCell(new Cell("Actual Delivery"));
            table.addCell(new Cell("Delay"));

            for (Shipment shipment : shipments) {

                boolean delayed =
                        isDelayed(shipment);

                if (!delayed) {
                    continue;
                }

                table.addCell(
                        new Cell(safe(shipment.getTrackingNumber()))
                );

                table.addCell(
                        new Cell(safe(shipment.getStatus()))
                );

                table.addCell(
                        new Cell(
                                shipment.getEstimatedDeliveryDate()
                                        == null
                                        ? ""
                                        : shipment
                                        .getEstimatedDeliveryDate()
                                        .toString()
                        )
                );

                table.addCell(
                        new Cell(
                                shipment.getActualDeliveryDate()
                                        == null
                                        ? ""
                                        : shipment
                                        .getActualDeliveryDate()
                                        .toString()
                        )
                );

                table.addCell(
                        new Cell("DELAYED")
                );
            }

            document.add(table);

            document.close();

            return output.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate delay PDF",
                    e
            );
        }
    }

    // =========================================================
    // DELAY EXCEL
    // =========================================================

    private byte[] generateDelayExcel(
            List<Shipment> shipments
    ) {

        try {

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet =
                    workbook.createSheet("Delay Analysis");

            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("Tracking Number");
            header.createCell(1).setCellValue("Status");
            header.createCell(2).setCellValue("Estimated Delivery");
            header.createCell(3).setCellValue("Actual Delivery");
            header.createCell(4).setCellValue("Delay Status");

            int rowNumber = 1;

            for (Shipment shipment : shipments) {

                if (!isDelayed(shipment)) {
                    continue;
                }

                Row row =
                        sheet.createRow(rowNumber++);

                row.createCell(0).setCellValue(
                        safe(shipment.getTrackingNumber())
                );

                row.createCell(1).setCellValue(
                        safe(shipment.getStatus())
                );

                row.createCell(2).setCellValue(
                        shipment.getEstimatedDeliveryDate()
                                == null
                                ? ""
                                : shipment
                                .getEstimatedDeliveryDate()
                                .toString()
                );

                row.createCell(3).setCellValue(
                        shipment.getActualDeliveryDate()
                                == null
                                ? ""
                                : shipment
                                .getActualDeliveryDate()
                                .toString()
                );

                row.createCell(4).setCellValue(
                        "DELAYED"
                );
            }

            autoSize(sheet, 5);

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            workbook.write(output);
            workbook.close();

            return output.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate delay Excel",
                    e
            );
        }
    }

    // =========================================================
    // DELAY CALCULATION
    // =========================================================

    private boolean isDelayed(
            Shipment shipment
    ) {

        if (shipment.getEstimatedDeliveryDate() == null) {
            return false;
        }

        if ("DELIVERED".equalsIgnoreCase(
                shipment.getStatus())) {

            if (shipment.getActualDeliveryDate() == null) {
                return false;
            }

            return shipment.getActualDeliveryDate()
                    .isAfter(
                            shipment.getEstimatedDeliveryDate()
                    );
        }

        return LocalDateTime.now()
                .isAfter(
                        shipment.getEstimatedDeliveryDate()
                );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void autoSize(
            Sheet sheet,
            int columns
    ) {

        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
