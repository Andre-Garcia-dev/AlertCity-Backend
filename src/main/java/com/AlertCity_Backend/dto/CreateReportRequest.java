package com.AlertCity_Backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Datos para registrar un nuevo reporte ciudadano")
public record CreateReportRequest(
        @Schema(description = "UUID del usuario (opcional si se deduce del JWT)", nullable = true)
        UUID userId,

        @Schema(description = "Título breve de la problemática", example = "Bache profundo frente a farmacia")
        @NotBlank(message = "El título del reporte es obligatorio")
        @Size(max = 150, message = "El título no puede superar 150 caracteres")
        String title,

        @Schema(description = "Descripción detallada del incidente")
        @NotBlank(message = "La descripción es obligatoria")
        String description,

        @Schema(description = "Categoría del catálogo (VIALIDAD, ALUMBRADO, AGUA, LIMPIEZA, SEGURIDAD, AREAS_VERDES, ANIMALES, OTRO)", example = "VIALIDAD")
        @NotBlank(message = "La categoría es obligatoria")
        String category,

        @Schema(description = "Indica si representa riesgo o peligro inminente", example = "true")
        @NotNull(message = "El campo urgent es obligatorio")
        Boolean urgent,

        @Schema(description = "Latitud GPS (WGS 84)", example = "20.1458")
        Double latitude,

        @Schema(description = "Longitud GPS (WGS 84)", example = "-101.1923")
        Double longitude,

        @Schema(description = "Referencia textual de ubicación", example = "Calle Juárez esquina Hidalgo")
        @Size(max = 255, message = "La referencia no puede superar 255 caracteres")
        String addressReference
) {
}
