package com.AlertCity_Backend.dto;

import java.util.UUID;

public record CreateReportRequest(
        UUID userId,
        String title,
        String description,
        String category,
        Boolean urgent,
        Double latitude,
        Double longitude,
        String addressReference
) {
}
