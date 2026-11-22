package com.AlertCity_Backend.controller;

import com.AlertCity_Backend.dto.CreateReportRequest;
import com.AlertCity_Backend.dto.ReportResponse;
import com.AlertCity_Backend.dto.UpdateReportStateRequest;
import com.AlertCity_Backend.model.Category;
import com.AlertCity_Backend.model.ReportState;
import com.AlertCity_Backend.model.User;
import com.AlertCity_Backend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(origins = "*")
@Tag(name = "Reportes", description = "Gestión de reportes ciudadanos, consultas espaciales PostGIS, filtros y evidencias fotográficas")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Listar reportes con filtros opcionales", description = "Devuelve los reportes ordenados cronológicamente (más recientes primero) filtrando por usuario, categoría, estado, urgencia o rango de fechas.")
    @GetMapping
    public ResponseEntity<List<ReportResponse>> getReports(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) ReportState state,
            @RequestParam(required = false) Boolean urgent,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate
    ) {
        return ResponseEntity.ok(
                reportService.getFilteredReports(userId, category, state, urgent, fromDate, toDate)
        );
    }

    @Operation(summary = "Listar reportes paginados", description = "Devuelve una página de reportes filtrados dinámicamente.")
    @GetMapping("/page")
    public ResponseEntity<Page<ReportResponse>> getReportsPage(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) ReportState state,
            @RequestParam(required = false) Boolean urgent,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @PageableDefault(size = 10, sort = "dateTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                reportService.getFilteredReportsPage(userId, category, state, urgent, fromDate, toDate, pageable)
        );
    }

    @Operation(summary = "Buscar reportes cercanos (PostGIS)", description = "Encuentra incidencias dentro de un radio en kilómetros usando ST_DWithin y el índice espacial GiST.")
    @GetMapping("/nearby")
    public ResponseEntity<List<ReportResponse>> getNearbyReports(
            @Parameter(description = "Latitud GPS de referencia", example = "20.1458") @RequestParam double lat,
            @Parameter(description = "Longitud GPS de referencia", example = "-101.1923") @RequestParam double lng,
            @Parameter(description = "Radio de búsqueda en kilómetros", example = "3.0") @RequestParam(defaultValue = "3.0") double radiusKm
    ) {
        return ResponseEntity.ok(reportService.getNearbyReports(lat, lng, radiusKm));
    }

    @Operation(summary = "Obtener reporte por ID", description = "Devuelve el detalle completo de un reporte por su identificador UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reporte encontrado"),
            @ApiResponse(responseCode = "404", description = "Reporte no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable UUID id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @Operation(summary = "Crear un nuevo reporte", description = "Registra un incidente ciudadano con coordenadas WGS 84. Si no se envía userId, se usa el del token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reporte creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PostMapping
    public ResponseEntity<ReportResponse> saveReport(
            @Valid @RequestBody CreateReportRequest createReportRequest,
            @AuthenticationPrincipal User currentUser
    ) {
        ReportResponse response = reportService.createReport(createReportRequest, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Actualizar estado de un reporte (RBAC)", description = "Permite a administradores u operadores departamentales autorizados cambiar el estado del reporte.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Rol sin permisos para gestionar este reporte"),
            @ApiResponse(responseCode = "404", description = "Reporte no encontrado")
    })
    @PatchMapping("/{id}/state")
    @PreAuthorize("hasAnyRole('ADMIN', 'VIALIDAD', 'ALUMBRADO', 'AGUA_DRENAJE', 'LIMPIEZA', 'SEGURIDAD', 'AREAS_VERDES', 'ANIMALES')")
    public ResponseEntity<ReportResponse> updateReportState(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReportStateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(reportService.updateReportState(id, request.state(), currentUser));
    }

    @Operation(summary = "Subir evidencia fotográfica", description = "Adjunta una imagen JPEG, PNG o WebP (máx. 10 MB) a un reporte existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evidencia subida y asociada al reporte"),
            @ApiResponse(responseCode = "400", description = "Archivo vacío o formato no permitido"),
            @ApiResponse(responseCode = "404", description = "Reporte no encontrado"),
            @ApiResponse(responseCode = "413", description = "Archivo excede el límite permitido")
    })
    @PostMapping(value = "/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponse> uploadReportMedia(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(reportService.uploadReportMedia(id, file));
    }
}
