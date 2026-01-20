package com.example.openmarket.infrastructure.adapter;

import com.example.openmarket.application.exception.ImageNotFoundException;
import com.example.openmarket.application.exception.StorageException;
import com.example.openmarket.application.port.StoragePort;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class LocalFileStorageAdapter implements StoragePort {

    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorageAdapter.class);

    private final Path uploadDirectory;

    public LocalFileStorageAdapter(
            @Value("${app.storage.upload-dir:./uploads}") String uploadDir) {
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadDirectory);
            logger.info("Storage directory initialized at: {}", uploadDirectory);
        } catch (IOException e) {
            throw new StorageException("Could not create upload directory", e);
        }
    }

    @Override
    public String store(InputStream inputStream, String originalFilename,
                        String contentType, long fileSize) {
        try {
            // Generate unique filename to prevent collisions
            String extension = getExtension(originalFilename);
            String storedFilename = UUID.randomUUID() + extension;

            // Validate and resolve path (prevents path traversal)
            Path targetPath = uploadDirectory.resolve(storedFilename).normalize();
            if (!targetPath.startsWith(uploadDirectory)) {
                throw new StorageException("Invalid file path detected");
            }

            // Copy file to storage
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            logger.debug("Stored file: {} as {}", originalFilename, storedFilename);

            return storedFilename;

        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + originalFilename, e);
        }
    }

    @Override
    public InputStream retrieve(String filename) {
        String sanitizedFilename = sanitizeFilename(filename);
        Path filePath = uploadDirectory.resolve(sanitizedFilename).normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            throw new ImageNotFoundException(filename);
        }

        if (!Files.exists(filePath)) {
            throw new ImageNotFoundException(filename);
        }

        try {
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new StorageException("Failed to retrieve file: " + filename, e);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            String sanitizedFilename = sanitizeFilename(filename);
            Path filePath = uploadDirectory.resolve(sanitizedFilename).normalize();

            if (!filePath.startsWith(uploadDirectory)) {
                throw new StorageException("Invalid file path detected");
            }

            Files.deleteIfExists(filePath);
            logger.debug("Deleted file: {}", sanitizedFilename);

        } catch (IOException e) {
            throw new StorageException("Failed to delete file: " + filename, e);
        }
    }

    @Override
    public boolean exists(String filename) {
        String sanitizedFilename = sanitizeFilename(filename);
        Path filePath = uploadDirectory.resolve(sanitizedFilename).normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            return false;
        }

        return Files.exists(filePath);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String sanitizeFilename(String filename) {
        // Remove any path components, keeping only the filename
        return Paths.get(filename).getFileName().toString();
    }
}
