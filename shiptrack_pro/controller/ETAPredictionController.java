package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.ETAPredictionResponse;
import com.shiptrack.shiptrack_pro.service.ETAPredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eta")
public class ETAPredictionController {

    private final ETAPredictionService etaService;

    public ETAPredictionController(
            ETAPredictionService etaService) {

        this.etaService = etaService;
    }

    @PostMapping("/{shipmentId}/predict")
    public ResponseEntity<ETAPredictionResponse>
    predictETA(@PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                etaService.predictETA(shipmentId));
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<ETAPredictionResponse>
    getETA(@PathVariable Long shipmentId) {

        return ResponseEntity.ok(
                etaService.getPrediction(shipmentId));
    }
}
