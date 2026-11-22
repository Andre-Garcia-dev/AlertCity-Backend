package com.AlertCity_Backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AlertCity REST API",
                version = "2.0",
                description = "API REST de AlertCity con autenticación JWT, RBAC, consultas espaciales PostGIS y carga de evidencias fotográficas.",
                contact = @Contact(name = "Equipo Backend AlertCity")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingresa el Access Token JWT obtenido en /api/v1/auth/login o /api/v1/auth/register"
)
public class OpenApiConfig {
}
