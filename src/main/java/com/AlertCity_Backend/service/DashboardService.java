package com.AlertCity_Backend.service;

import com.AlertCity_Backend.dto.DashboardResponse;
import com.AlertCity_Backend.dto.ReportResponse;
import com.AlertCity_Backend.model.ReportState;
import com.AlertCity_Backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {
    private final UserRepository userRepository;
    private final ReportService reportService;

    public DashboardService(UserRepository userRepository, ReportService reportService) {
        this.userRepository = userRepository;
        this.reportService = reportService;
    }

    public DashboardResponse getDashboardData(UUID userId){
        List<ReportResponse> myReports = reportService.getReportsByUser(userId);
        List<ReportResponse> allReports = reportService.getAllReports();

        int myActiveReportsCount = (int) myReports.stream()
                .filter(r -> r.state() != ReportState.RESUELTO)
                .count();
        int mySolvedReportsCount = (int) myReports.stream()
                .filter(r -> r.state() == ReportState.RESUELTO)
                .count();

        return new DashboardResponse(
                myActiveReportsCount,
                mySolvedReportsCount,
                myReports,
                allReports);
    }

}
