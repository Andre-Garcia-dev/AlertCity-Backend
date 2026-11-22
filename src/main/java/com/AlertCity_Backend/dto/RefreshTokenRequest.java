package com.AlertCity_Backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud de renovación de Access Token usando un Refresh Token válido")
public record RefreshTokenRequest(
        @Schema(description = "Token de actualización emitido al iniciar sesión")
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken
) {
}
