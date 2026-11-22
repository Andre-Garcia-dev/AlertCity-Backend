package com.AlertCity_Backend.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), request, "not-found");
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleConflict(DuplicateResourceException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.CONFLICT, "Conflicto de recurso duplicado", ex.getMessage(), request, "conflict");
    }

    @ExceptionHandler({
            BadRequestException.class,
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ProblemDetail handleBadRequest(Exception ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage(), request, "bad-request");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail problem = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Error de validación",
                "Uno o más campos de la petición no son válidos",
                request,
                "validation-error"
        );
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler({AuthenticationException.class, JwtException.class})
    public ProblemDetail handleUnauthorized(Exception ex, HttpServletRequest request) {
        return buildProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "No autenticado",
                ex.getMessage() != null ? ex.getMessage() : "Credenciales o token inválidos",
                request,
                "unauthorized"
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildProblemDetail(
                HttpStatus.FORBIDDEN,
                "Acceso denegado",
                ex.getMessage() != null ? ex.getMessage() : "No cuentas con permisos suficientes para realizar esta acción",
                request,
                "forbidden"
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return buildProblemDetail(
                HttpStatusCode.valueOf(413),
                "Archivo demasiado grande",
                "El archivo excede el tamaño máximo permitido de 10 MB",
                request,
                "payload-too-large"
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor",
                "Ocurrió un error inesperado procesando la solicitud",
                request,
                "internal-error"
        );
    }

    private ProblemDetail buildProblemDetail(
            HttpStatusCode status,
            String title,
            String detail,
            HttpServletRequest request,
            String typeSlug
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://alertcity.app/errors/" + typeSlug));
        if (request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
