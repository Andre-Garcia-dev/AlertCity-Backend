package com.AlertCity_Backend.dto;

import com.AlertCity_Backend.model.ReportState;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Solicitud para actualizar el estado de atención de un reporte")
public record UpdateReportStateRequest(
        @Schema(description = "Nuevo estado del reporte", example = "EN_PROCESO")
        @NotNull(message = "El nuevo estado es obligatorio")
        ReportState state
) {
}
