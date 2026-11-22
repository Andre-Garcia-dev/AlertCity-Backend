package com.AlertCity_Backend.service;

import com.AlertCity_Backend.dto.CreateReportRequest;
import com.AlertCity_Backend.dto.ReportResponse;
import com.AlertCity_Backend.model.Category;
import com.AlertCity_Backend.model.Report;
import com.AlertCity_Backend.model.ReportState;
import com.AlertCity_Backend.model.User;
import com.AlertCity_Backend.repository.ReportRepository;
import com.AlertCity_Backend.repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public ReportService(
            ReportRepository reportRepository,
            UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    public List<ReportResponse> getAllReports(){
        return reportRepository.findAllByOrderByDateTimeDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ReportResponse> getReportsByUser(UUID userId) {
        return reportRepository.findByUser_IdUserOrderByDateTimeDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ReportResponse createReport(CreateReportRequest request){
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User with ID: " +  request.userId() + " not found"));

        Point location = null;
        if (request.latitude() != null && request.longitude() != null) {
            location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        }

        Category category = Category.valueOf(request.category().toUpperCase());

        return mapToResponse(
                reportRepository.save(
                        new Report(
                                user,
                                request.title(),
                                request.description(),
                                category,
                                request.urgent(),
                                LocalDateTime.now(),
                                location,
                                request.addressReference(),
                                ReportState.ENVIADO
                        )
                )
        );
    }

    public ReportResponse mapToResponse(Report report){
        Double latitude = (report.getLocation() != null) ? report.getLocation().getY() : null;
        Double longitude = (report.getLocation() != null) ? report.getLocation().getX() : null;

        return new ReportResponse(
                report.getId(),
                report.getUser()!=null ? report.getUser().getIdUser() : null,
                report.getTitle(),
                report.getDescription(),
                report.getCategory(),
                report.getUrgent(),
                report.getDateTime(),
                latitude,
                longitude,
                report.getAddressReference(),
                report.getState()
        );
    }
}