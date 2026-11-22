package com.AlertCity_Backend.service;

import com.AlertCity_Backend.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class MediaStorageService {

    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/jpg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final Path uploadRootPath;

    public MediaStorageService(@Value("${app.upload.dir:uploads/reports}") String uploadDir) {
        this.uploadRootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String storeReportImage(UUID reportId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo de imagen es obligatorio y no puede estar vacío");
        }

        String contentType = file.getContentType();
        String normalizedType = contentType != null ? contentType.toLowerCase() : "";
        String extension = ALLOWED_CONTENT_TYPES.get(normalizedType);
        if (extension == null) {
            throw new BadRequestException("Formato de imagen no permitido. Solo se aceptan JPEG, PNG o WebP");
        }

        String filename = reportId + "_" + UUID.randomUUID() + extension;
        try {
            Files.createDirectories(uploadRootPath);
            Path destination = uploadRootPath.resolve(filename).normalize();
            if (!destination.startsWith(uploadRootPath)) {
                throw new BadRequestException("Ruta de almacenamiento inválida");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/reports/" + filename;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo almacenar la imagen de evidencia del reporte", ex);
        }
    }
}
