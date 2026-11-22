package com.AlertCity_Backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos requeridos para registrar un nuevo usuario ciudadano")
public record RegisterRequest(
        @Schema(description = "Nombre de usuario único", example = "carlos_vecino")
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(min = 3, max = 100, message = "El username debe tener entre 3 y 100 caracteres")
        String username,

        @Schema(description = "Correo electrónico único", example = "carlos@ejemplo.com")
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato del correo electrónico no es válido")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres")
        String email,

        @Schema(description = "Contraseña en texto plano (mínimo 6 caracteres)", example = "Secreta2026!")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
        String password,

        @Schema(description = "Nombre(s)", example = "Carlos")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String firstName,

        @Schema(description = "Apellidos", example = "Ramírez")
        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
        String lastName
) {
}
