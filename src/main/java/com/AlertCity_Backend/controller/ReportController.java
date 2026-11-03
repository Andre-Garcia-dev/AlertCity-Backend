package com.AlertCity_Backend.controller;

import com.AlertCity_Backend.dto.CreateReportRequest;
import com.AlertCity_Backend.dto.ReportResponse;
import com.AlertCity_Backend.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(origins = "*")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getReports(@RequestParam(required = false) UUID userId) {
        if (userId != null) {
            return ResponseEntity.ok(reportService.getReportsByUser(userId));
        }
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @PostMapping
    public ResponseEntity<ReportResponse> saveReport(@Valid @RequestBody CreateReportRequest createReportRequest) {
        ReportResponse response = reportService.createReport(createReportRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
