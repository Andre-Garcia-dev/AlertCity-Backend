package com.AlertCity_Backend;

import com.AlertCity_Backend.controller.AuthController;
import com.AlertCity_Backend.controller.ReportController;
import com.AlertCity_Backend.dto.AuthResponse;
import com.AlertCity_Backend.dto.CreateReportRequest;
import com.AlertCity_Backend.dto.LoginRequest;
import com.AlertCity_Backend.dto.RefreshTokenRequest;
import com.AlertCity_Backend.dto.RegisterRequest;
import com.AlertCity_Backend.dto.ReportResponse;
import com.AlertCity_Backend.exception.BadRequestException;
import com.AlertCity_Backend.exception.DuplicateResourceException;
import com.AlertCity_Backend.exception.GlobalExceptionHandler;
import com.AlertCity_Backend.exception.ResourceNotFoundException;
import com.AlertCity_Backend.model.Category;
import com.AlertCity_Backend.model.Report;
import com.AlertCity_Backend.model.ReportState;
import com.AlertCity_Backend.model.Rol;
import com.AlertCity_Backend.model.User;
import com.AlertCity_Backend.repository.ReportRepository;
import com.AlertCity_Backend.repository.UserRepository;
import com.AlertCity_Backend.security.JwtService;
import com.AlertCity_Backend.service.AuthService;
import com.AlertCity_Backend.service.MediaStorageService;
import com.AlertCity_Backend.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class Phase2SecurityAndServiceTests {

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthService authServiceMock;

    @Mock
    private ReportService reportServiceMock;

    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;
    private ReportService reportService;
    private MediaStorageService mediaStorageService;

    @TempDir
    Path tempDir;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 2592000000L);

        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder, jwtService, authenticationManager);
        mediaStorageService = new MediaStorageService(tempDir.toString());
        reportService = new ReportService(reportRepository, userRepository, mediaStorageService);
    }

    @Test
    void jwtServiceShouldGenerateAndValidateAccessAndRefreshTokens() {
        User user = buildUser(UUID.randomUUID(), "laura_ciudadana", Rol.CIUDADANO);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        assertEquals("laura_ciudadana", jwtService.extractUsername(accessToken));
        assertEquals("ACCESS", jwtService.extractTokenType(accessToken));
        assertEquals("REFRESH", jwtService.extractTokenType(refreshToken));

        assertTrue(jwtService.isAccessTokenValid(accessToken, user));
        assertFalse(jwtService.isRefreshTokenValid(accessToken, user));
        assertTrue(jwtService.isRefreshTokenValid(refreshToken, user));
        assertFalse(jwtService.isAccessTokenValid(refreshToken, user));
    }

    @Test
    void authServiceShouldRegisterLoginAndRefreshTokens() {
        RegisterRequest registerReq = new RegisterRequest(
                "carlos_vecino",
                "Carlos@Ejemplo.com",
                "Secreta123",
                "Carlos",
                "Ramírez"
        );

        when(userRepository.existsByUser("carlos_vecino")).thenReturn(false);
        when(userRepository.existsByEmail("carlos@ejemplo.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setIdUser(UUID.randomUUID());
            return saved;
        });

        AuthResponse registered = authService.register(registerReq);
        assertEquals("Bearer", registered.tokenType());
        assertEquals(Rol.CIUDADANO, registered.rol());
        assertEquals("carlos@ejemplo.com", registered.email());
        assertNotNull(registered.accessToken());
        assertNotNull(registered.refreshToken());

        when(userRepository.existsByUser("carlos_vecino")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> authService.register(registerReq));

        User persisted = buildUser(registered.userId(), "carlos_vecino", Rol.CIUDADANO);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(persisted, null, persisted.getAuthorities()));

        AuthResponse loggedIn = authService.login(new LoginRequest("carlos_vecino", "Secreta123"));
        assertEquals("carlos_vecino", loggedIn.username());

        when(userRepository.findByUser("carlos_vecino")).thenReturn(Optional.of(persisted));
        AuthResponse refreshed = authService.refreshToken(new RefreshTokenRequest(loggedIn.refreshToken()));
        assertTrue(jwtService.isAccessTokenValid(refreshed.accessToken(), persisted));

        assertThrows(BadCredentialsException.class,
                () -> authService.refreshToken(new RefreshTokenRequest(loggedIn.accessToken())));
    }

    @Test
    void reportServiceShouldResolveJwtAuthorValidateNearbyAndEnforceRbac() {
        UUID citizenId = UUID.randomUUID();
        User citizen = buildUser(citizenId, "laura_ciudadana", Rol.CIUDADANO);
        User vialidadOperator = buildUser(UUID.randomUUID(), "carlos_vialidad", Rol.VIALIDAD);
        User admin = buildUser(UUID.randomUUID(), "admin_city", Rol.ADMIN);

        when(userRepository.findById(citizenId)).thenReturn(Optional.of(citizen));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            if (report.getId() == null) {
                report.setId(UUID.randomUUID());
            }
            return report;
        });

        CreateReportRequest createReq = new CreateReportRequest(
                null,
                "Bache profundo",
                "Afecta el carril derecho",
                "VIALIDAD",
                true,
                20.1458,
                -101.1923,
                "Av. Juárez #10"
        );

        ReportResponse created = reportService.createReport(createReq, citizen);
        assertEquals(citizenId, created.userId());
        assertEquals(Category.VIALIDAD, created.category());
        assertEquals(ReportState.ENVIADO, created.state());

        Report vialidadReport = buildReport(UUID.randomUUID(), citizen, Category.VIALIDAD, ReportState.ENVIADO);
        Report aguaReport = buildReport(UUID.randomUUID(), citizen, Category.AGUA, ReportState.ENVIADO);

        when(reportRepository.findById(vialidadReport.getId())).thenReturn(Optional.of(vialidadReport));
        when(reportRepository.findById(aguaReport.getId())).thenReturn(Optional.of(aguaReport));

        assertThrows(AccessDeniedException.class,
                () -> reportService.updateReportState(vialidadReport.getId(), ReportState.EN_PROCESO, citizen));

        assertThrows(AccessDeniedException.class,
                () -> reportService.updateReportState(aguaReport.getId(), ReportState.EN_PROCESO, vialidadOperator));

        ReportResponse updatedByOperator = reportService.updateReportState(
                vialidadReport.getId(), ReportState.EN_PROCESO, vialidadOperator
        );
        assertEquals(ReportState.EN_PROCESO, updatedByOperator.state());

        ReportResponse updatedByAdmin = reportService.updateReportState(
                aguaReport.getId(), ReportState.RESUELTO, admin
        );
        assertEquals(ReportState.RESUELTO, updatedByAdmin.state());

        when(reportRepository.findNearbyReports(20.1458, -101.1923, 2500.0))
                .thenReturn(List.of(vialidadReport));

        List<ReportResponse> nearby = reportService.getNearbyReports(20.1458, -101.1923, 2.5);
        assertEquals(1, nearby.size());
        verify(reportRepository).findNearbyReports(20.1458, -101.1923, 2500.0);

        assertThrows(BadRequestException.class, () -> reportService.getNearbyReports(120.0, -101.1923, 2.0));
        assertThrows(BadRequestException.class, () -> reportService.getNearbyReports(20.1458, -101.1923, 0.0));

        PageRequest pageable = PageRequest.of(0, 10);
        when(reportRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Report>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(vialidadReport), pageable, 1));
        Page<ReportResponse> page = reportService.getFilteredReportsPage(
                citizenId, Category.VIALIDAD, ReportState.EN_PROCESO, true, null, null, pageable
        );
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void mediaStorageServiceShouldPersistValidImagesAndRejectUnsupportedTypes() throws Exception {
        UUID reportId = UUID.randomUUID();
        MockMultipartFile validJpeg = new MockMultipartFile(
                "file",
                "bache.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{10, 20, 30}
        );

        String storedPath = mediaStorageService.storeReportImage(reportId, validJpeg);
        assertTrue(storedPath.startsWith("/uploads/reports/" + reportId + "_"));
        assertTrue(storedPath.endsWith(".jpg"));

        String filename = storedPath.substring("/uploads/reports/".length());
        assertTrue(Files.exists(tempDir.resolve(filename)));

        MockMultipartFile invalidText = new MockMultipartFile(
                "file",
                "script.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hola".getBytes()
        );
        assertThrows(BadRequestException.class, () -> mediaStorageService.storeReportImage(reportId, invalidText));
    }

    @Test
    void globalExceptionHandlerShouldReturnRfc7807ProblemDetails() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authServiceMock), new ReportController(reportServiceMock))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "email": "no-es-correo",
                                  "password": "123",
                                  "firstName": "",
                                  "lastName": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validación"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        when(authServiceMock.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usernameOrEmail": "laura_ciudadana",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        when(authServiceMock.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("Usuario duplicado"));
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "laura_ciudadana",
                                  "email": "laura@ejemplo.com",
                                  "password": "Password123",
                                  "firstName": "Laura",
                                  "lastName": "Sánchez"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        UUID missingId = UUID.randomUUID();
        when(reportServiceMock.getReportById(missingId))
                .thenThrow(new ResourceNotFoundException("Reporte no encontrado"));
        mockMvc.perform(get("/api/v1/reports/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        when(reportServiceMock.getNearbyReports(20.0, -101.0, 1.0))
                .thenThrow(new AccessDeniedException("Sin permisos"));
        mockMvc.perform(get("/api/v1/reports/nearby")
                        .param("lat", "20.0")
                        .param("lng", "-101.0")
                        .param("radiusKm", "1.0"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    private User buildUser(UUID id, String username, Rol rol) {
        User user = new User(username, username + "@alertcity.com", passwordEncoder.encode("password"), rol, "Nombre", "Apellido");
        user.setIdUser(id);
        return user;
    }

    private Report buildReport(UUID reportId, User user, Category category, ReportState state) {
        Point point = geometryFactory.createPoint(new Coordinate(-101.1923, 20.1458));
        Report report = new Report(
                user,
                "Título de prueba",
                "Descripción de prueba",
                category,
                true,
                LocalDateTime.now(),
                point,
                "Centro",
                state
        );
        report.setId(reportId);
        return report;
    }
}
