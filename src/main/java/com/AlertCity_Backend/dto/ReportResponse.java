package com.AlertCity_Backend.dto;

import com.AlertCity_Backend.model.Category;
import com.AlertCity_Backend.model.ReportState;

import java.time.LocalDateTime;
import java.util.UUID;

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
        ReportState state
) {
}