package com.AlertCity_Backend.dto;

import com.AlertCity_Backend.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Respuesta de autenticación con tokens JWT y datos del usuario")
public record AuthResponse(
        @Schema(description = "Token JWT de acceso")
        String accessToken,

        @Schema(description = "Token JWT de renovación")
        String refreshToken,

        @Schema(description = "Tipo de token", example = "Bearer")
        String tokenType,

        @Schema(description = "Tiempo de expiración del access token en milisegundos", example = "3600000")
        long expiresIn,

        @Schema(description = "Identificador único del usuario")
        UUID userId,

        @Schema(description = "Nombre de usuario")
        String username,

        @Schema(description = "Correo electrónico")
        String email,

        @Schema(description = "Rol asignado en la plataforma")
        Rol rol,

        @Schema(description = "Nombre(s)")
        String firstName,

        @Schema(description = "Apellidos")
        String lastName
) {
}
