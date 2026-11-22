package com.AlertCity_Backend.controller;

import com.AlertCity_Backend.dto.DashboardResponse;
import com.AlertCity_Backend.model.User;
import com.AlertCity_Backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard (BFF)", description = "Endpoint consolidado para pantalla principal móvil")
public class DashboardController {

    private static final UUID DEMO_FALLBACK_USER_ID = UUID.fromString("11111111-1111-4111-a111-111111111111");

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(
            summary = "Obtener datos del Dashboard",
            description = "Retorna métricas personales, reportes del usuario autenticado (o userId indicado) y feed comunitario."
    )
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboardData(
            @RequestParam(required = false) UUID userId,
            @AuthenticationPrincipal User currentUser
    ) {
        UUID targetId = resolveTargetUserId(userId, currentUser);
        DashboardResponse response = dashboardService.getDashboardData(targetId);
        return ResponseEntity.ok(response);
    }

    private UUID resolveTargetUserId(UUID requestedUserId, User currentUser) {
        if (requestedUserId != null) {
            return requestedUserId;
        }
        if (currentUser != null && currentUser.getIdUser() != null) {
            return currentUser.getIdUser();
        }
        return DEMO_FALLBACK_USER_ID;
    }
}
