package com.AlertCity_Backend;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AlertCityBackendApplicationTests {

    @TempDir
    static Path tempUploadDir;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("app.upload.dir", () -> tempUploadDir.toAbsolutePath().toString());
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setupMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldRegisterLoginAndRefreshJwtTokens() throws Exception {
        String registerPayload = """
                {
                  "username": "nuevo_ciudadano_test",
                  "email": "nuevo.test@alertcity.com",
                  "password": "Password123!",
                  "firstName": "Test",
                  "lastName": "Ciudadano"
                }
                """;

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.rol", is("CIUDADANO")))
                .andReturn();

        String refreshToken = JsonPath.read(registerResult.getResponse().getContentAsString(), "$.refreshToken");

        String loginPayload = """
                {
                  "usernameOrEmail": "nuevo_ciudadano_test",
                  "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.username", is("nuevo_ciudadano_test")));

        String refreshPayload = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()));
    }

    @Test
    void shouldEnforceJwtAuthenticationAndRbac() throws Exception {
        mockMvc.perform(get("/api/v1/reports"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));

        String citizenToken = loginAndGetAccessToken("laura_ciudadana", "password");
        String vialidadToken = loginAndGetAccessToken("carlos_vialidad", "password");

        mockMvc.perform(get("/api/v1/dashboard")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allReports", hasSize(greaterThan(0))));

        String vialidadReportId = "c0000001-0000-4000-8000-000000000001";
        String statePayload = """
                {
                  "state": "EN_PROCESO"
                }
                """;

        mockMvc.perform(patch("/api/v1/reports/{id}/state", vialidadReportId)
                        .header("Authorization", "Bearer " + citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statePayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        mockMvc.perform(patch("/api/v1/reports/{id}/state", vialidadReportId)
                        .header("Authorization", "Bearer " + vialidadToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is("EN_PROCESO")));
    }

    @Test
    void shouldQueryNearbyPostGisReportsFiltersAndUploadMedia() throws Exception {
        String citizenToken = loginAndGetAccessToken("laura_ciudadana", "password");

        mockMvc.perform(get("/api/v1/reports/nearby")
                        .param("lat", "20.1458")
                        .param("lng", "-101.1923")
                        .param("radiusKm", "2.0")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));

        mockMvc.perform(get("/api/v1/reports")
                        .param("category", "VIALIDAD")
                        .param("urgent", "true")
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category", is("VIALIDAD")))
                .andExpect(jsonPath("$[0].urgent", is(true)));

        String reportId = "c0000001-0000-4000-8000-000000000005";
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "evidencia.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{1, 2, 3, 4, 5}
        );

        mockMvc.perform(multipart("/api/v1/reports/{id}/media", reportId)
                        .file(imageFile)
                        .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl", containsString("/uploads/reports/")));
    }

    private String loginAndGetAccessToken(String usernameOrEmail, String password) throws Exception {
        String payload = """
                {
                  "usernameOrEmail": "%s",
                  "password": "%s"
                }
                """.formatted(usernameOrEmail, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }
}
