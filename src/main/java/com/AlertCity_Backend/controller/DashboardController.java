package com.AlertCity_Backend.controller;

import com.AlertCity_Backend.dto.DashboardResponse;
import com.AlertCity_Backend.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboardData(@RequestParam(required = false) UUID userId) {
        // Para pruebas de la Demo: si el móvil no envía userId aún, usamos uno simulado de la semilla
        UUID targetId = (userId != null) ? userId : UUID.fromString("11111111-1111-4111-a111-111111111111");
        DashboardResponse response = dashboardService.getDashboardData(targetId);

        return ResponseEntity.ok(response);
    }
}
