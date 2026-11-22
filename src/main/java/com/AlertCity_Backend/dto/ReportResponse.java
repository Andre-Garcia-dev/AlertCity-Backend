package com.AlertCity_Backend.dto;

import com.AlertCity_Backend.model.Category;
import com.AlertCity_Backend.model.ReportState;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Representación de un reporte ciudadano")
public record ReportResponse(
        UUID reportId,
        UUID userId,
        String title,
        String description,
        Category category,
        Boolean urgent,
        LocalDateTime dateTime,
        Double latitude,
        Double longitude,
        String addressReference,
        ReportState state,
        String imageUrl
) {
    public ReportResponse(
            UUID reportId,
            UUID userId,
            String title,
            String description,
            Category category,
            Boolean urgent,
            LocalDateTime dateTime,
            Double latitude,
            Double longitude,
            String addressReference,
            ReportState state
    ) {
        this(reportId, userId, title, description, category, urgent, dateTime, latitude, longitude, addressReference, state, null);
    }
}
