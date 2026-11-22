package com.AlertCity_Backend.dto;

import java.util.List;

public record DashboardResponse(
        int myActiveReportsCount,
        int mySolvedReportsCount,
        List<ReportResponse> myReports,
        List<ReportResponse> allReports
) {}
