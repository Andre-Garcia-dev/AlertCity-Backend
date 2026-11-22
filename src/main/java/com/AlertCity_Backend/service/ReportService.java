package com.AlertCity_Backend.service;

import com.AlertCity_Backend.dto.CreateReportRequest;
import com.AlertCity_Backend.dto.ReportResponse;
import com.AlertCity_Backend.exception.BadRequestException;
import com.AlertCity_Backend.exception.ResourceNotFoundException;
import com.AlertCity_Backend.model.Category;
import com.AlertCity_Backend.model.Report;
import com.AlertCity_Backend.model.ReportState;
import com.AlertCity_Backend.model.Rol;
import com.AlertCity_Backend.model.User;
import com.AlertCity_Backend.repository.ReportRepository;
import com.AlertCity_Backend.repository.ReportSpecification;
import com.AlertCity_Backend.repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportService {

    private static final double DEFAULT_LATITUDE = 20.1458;
    private static final double DEFAULT_LONGITUDE = -101.1923;

    private static final Map<Rol, Category> OPERATOR_CATEGORY_MAP = Map.of(
            Rol.VIALIDAD, Category.VIALIDAD,
            Rol.ALUMBRADO, Category.ALUMBRADO,
            Rol.AGUA_DRENAJE, Category.AGUA,
            Rol.LIMPIEZA, Category.LIMPIEZA,
            Rol.SEGURIDAD, Category.SEGURIDAD,
            Rol.AREAS_VERDES, Category.AREAS_VERDES,
            Rol.ANIMALES, Category.ANIMALES
    );

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final MediaStorageService mediaStorageService;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public ReportService(
            ReportRepository reportRepository,
            UserRepository userRepository,
            MediaStorageService mediaStorageService
    ) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.mediaStorageService = mediaStorageService;
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getAllReports() {
        return reportRepository.findAllByOrderByDateTimeDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByUser(UUID userId) {
        return reportRepository.findByUser_IdUserOrderByDateTimeDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportResponse getReportById(UUID reportId) {
        Report report = findReportOrThrow(reportId);
        return mapToResponse(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getFilteredReports(
            UUID userId,
            Category category,
            ReportState state,
            Boolean urgent,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {
        Specification<Report> spec = ReportSpecification.filterBy(userId, category, state, urgent, fromDate, toDate);
        return reportRepository.findAll(spec)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> getFilteredReportsPage(
            UUID userId,
            Category category,
            ReportState state,
            Boolean urgent,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    ) {
        Specification<Report> spec = ReportSpecification.filterBy(userId, category, state, urgent, fromDate, toDate);
        return reportRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getNearbyReports(double lat, double lng, double radiusKm) {
        validateCoordinates(lat, lng);
        if (radiusKm <= 0 || radiusKm > 100) {
            throw new BadRequestException("El radio de búsqueda debe estar entre 0 y 100 km");
        }
        double radiusMeters = radiusKm * 1000.0;
        return reportRepository.findNearbyReports(lat, lng, radiusMeters)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ReportResponse createReport(CreateReportRequest request) {
        return createReport(request, null);
    }

    @Transactional
    public ReportResponse createReport(CreateReportRequest request, User authenticatedUser) {
        User targetUser = resolveReportAuthor(request.userId(), authenticatedUser);

        double lat = request.latitude() != null ? request.latitude() : DEFAULT_LATITUDE;
        double lng = request.longitude() != null ? request.longitude() : DEFAULT_LONGITUDE;
        validateCoordinates(lat, lng);

        Point location = geometryFactory.createPoint(new Coordinate(lng, lat));
        Category category = parseCategory(request.category());

        Report saved = reportRepository.save(
                new Report(
                        targetUser,
                        request.title().trim(),
                        request.description().trim(),
                        category,
                        Boolean.TRUE.equals(request.urgent()),
                        LocalDateTime.now(),
                        location,
                        request.addressReference(),
                        ReportState.ENVIADO
                )
        );
        return mapToResponse(saved);
    }

    @Transactional
    public ReportResponse updateReportState(UUID reportId, ReportState newState, User actor) {
        if (actor == null) {
            throw new AccessDeniedException("Se requiere un usuario autenticado para cambiar el estado del reporte");
        }

        Report report = findReportOrThrow(reportId);
        if (actor.getRol() != Rol.ADMIN) {
            Category allowedCategory = OPERATOR_CATEGORY_MAP.get(actor.getRol());
            if (allowedCategory == null || allowedCategory != report.getCategory()) {
                throw new AccessDeniedException(
                        "El rol " + actor.getRol() + " no está autorizado para gestionar reportes de " + report.getCategory()
                );
            }
        }

        report.setState(newState);
        return mapToResponse(reportRepository.save(report));
    }

    @Transactional
    public ReportResponse uploadReportMedia(UUID reportId, MultipartFile file) {
        Report report = findReportOrThrow(reportId);
        String imageUrl = mediaStorageService.storeReportImage(reportId, file);
        report.setImageUrl(imageUrl);
        return mapToResponse(reportRepository.save(report));
    }

    public ReportResponse mapToResponse(Report report) {
        Double latitude = (report.getLocation() != null) ? report.getLocation().getY() : null;
        Double longitude = (report.getLocation() != null) ? report.getLocation().getX() : null;

        return new ReportResponse(
                report.getId(),
                report.getUser() != null ? report.getUser().getIdUser() : null,
                report.getTitle(),
                report.getDescription(),
                report.getCategory(),
                report.getUrgent(),
                report.getDateTime(),
                latitude,
                longitude,
                report.getAddressReference(),
                report.getState(),
                report.getImageUrl()
        );
    }

    private Report findReportOrThrow(UUID reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con ID: " + reportId));
    }

    private User resolveReportAuthor(UUID requestedUserId, User authenticatedUser) {
        if (requestedUserId != null) {
            return userRepository.findById(requestedUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + requestedUserId));
        }
        if (authenticatedUser != null) {
            return userRepository.findById(authenticatedUser.getIdUser())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
        }
        throw new BadRequestException("Debe especificar un userId o iniciar sesión para crear un reporte");
    }

    private Category parseCategory(String rawCategory) {
        if (rawCategory == null || rawCategory.isBlank()) {
            throw new BadRequestException("La categoría del reporte es obligatoria");
        }
        try {
            return Category.valueOf(rawCategory.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Categoría inválida: '" + rawCategory + "'");
        }
    }

    private void validateCoordinates(double lat, double lng) {
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
            throw new BadRequestException("Coordenadas geográficas fuera de rango (latitud [-90, 90], longitud [-180, 180])");
        }
    }
}
