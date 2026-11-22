package com.AlertCity_Backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para iniciar sesión por username o email")
public record LoginRequest(
        @Schema(description = "Nombre de usuario o correo electrónico", example = "laura_ciudadana")
        @NotBlank(message = "El usuario o correo es obligatorio")
        String usernameOrEmail,

        @Schema(description = "Contraseña del usuario", example = "password")
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
