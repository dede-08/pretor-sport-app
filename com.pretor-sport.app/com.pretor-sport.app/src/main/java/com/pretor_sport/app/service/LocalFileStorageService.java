package com.pretor_sport.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.images.upload-dir}")
    private String uploadDir;

    @Value("${app.images.base-url}")
    private String baseUrl;

    @Value("${app.images.max-size:5MB}")
    private DataSize maxSize;

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        if (file.getSize() > maxSize.toBytes()) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido (" + maxSize.toMegabytes() + "MB)");
        }

        String contentType = file.getContentType();
        List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/webp", "image/gif");
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen (JPEG, PNG, WEBP, GIF)");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        int lastIndex = originalFileName.lastIndexOf(".");
        if (lastIndex >= 0) {
            fileExtension = originalFileName.substring(lastIndex);
        } else {
            fileExtension = "." + contentType.split("/")[1];
        }

        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(uniqueFileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Archivo guardado exitosamente: {}", uniqueFileName);
        return baseUrl + "/" + uniqueFileName;
    }

    @Override
    public void deleteFile(String fileUrl) throws IOException {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        Files.deleteIfExists(filePath);
        log.info("Archivo eliminado: {}", fileName);
    }
}
