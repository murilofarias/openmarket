package com.example.openmarket.application.service;

import com.example.openmarket.application.exception.FileSizeExceededException;
import com.example.openmarket.application.exception.ImageNotFoundException;
import com.example.openmarket.application.exception.InvalidFileTypeException;
import com.example.openmarket.application.exception.StorageException;
import com.example.openmarket.application.port.StoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Set;

@Service
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    private final StoragePort storagePort;
    private final long maxFileSize;

    public ImageService(
            StoragePort storagePort,
            @Value("${app.storage.max-file-size:10485760}") long maxFileSize) {
        this.storagePort = storagePort;
        this.maxFileSize = maxFileSize;
    }

    /**
     * Validates and uploads an image file.
     *
     * @param file the multipart file to upload
     * @return the URL of the stored image
     * @throws InvalidFileTypeException if file validation fails
     * @throws FileSizeExceededException if file exceeds max size
     * @throws StorageException if storage operation fails
     */
    public String uploadImage(MultipartFile file) {
        validateFile(file);

        try {
            String url = storagePort.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize()
            );

            logger.debug("Image stored successfully: {}", url);
            return url;

        } catch (InvalidFileTypeException | FileSizeExceededException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to store image: {}", e.getMessage());
            throw new StorageException("Failed to upload image", e);
        }
    }

    private void validateFile(MultipartFile file) {
        validateNotEmpty(file);
        validateSize(file);
        validateContentType(file);
        validateExtension(file);
        validateMagicBytes(file);
    }

    private void validateNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileTypeException("File cannot be empty");
        }
    }

    private void validateSize(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            throw new FileSizeExceededException(
                String.format("File size exceeds maximum allowed size of %d MB",
                              maxFileSize / (1024 * 1024)));
        }
    }

    private void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileTypeException(
                "Invalid file type. Allowed types: JPEG, PNG, GIF, WebP");
        }
    }

    private void validateExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (!hasAllowedExtension(originalFilename)) {
            throw new InvalidFileTypeException(
                "Invalid file extension. Allowed extensions: .jpg, .jpeg, .png, .gif, .webp");
        }
    }

    private boolean hasAllowedExtension(String filename) {
        if (filename == null) return false;
        String lowerFilename = filename.toLowerCase();
        return ALLOWED_EXTENSIONS.stream().anyMatch(lowerFilename::endsWith);
    }

    private void validateMagicBytes(MultipartFile file) {
        String contentType = file.getContentType();
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length < 4) {
                throw new InvalidFileTypeException("File is too small to be a valid image");
            }

            boolean valid = switch (contentType.toLowerCase()) {
                case "image/jpeg" ->
                    // JPEG: FF D8 FF
                    bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF;
                case "image/png" ->
                    // PNG: 89 50 4E 47
                    bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 &&
                    bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47;
                case "image/gif" ->
                    // GIF: 47 49 46 38
                    bytes[0] == (byte) 0x47 && bytes[1] == (byte) 0x49 &&
                    bytes[2] == (byte) 0x46 && bytes[3] == (byte) 0x38;
                case "image/webp" ->
                    // WebP: 52 49 46 46 ... 57 45 42 50 (RIFF...WEBP)
                    bytes.length >= 12 &&
                    bytes[0] == (byte) 0x52 && bytes[1] == (byte) 0x49 &&
                    bytes[2] == (byte) 0x46 && bytes[3] == (byte) 0x46 &&
                    bytes[8] == (byte) 0x57 && bytes[9] == (byte) 0x45 &&
                    bytes[10] == (byte) 0x42 && bytes[11] == (byte) 0x50;
                default -> false;
            };

            if (!valid) {
                throw new InvalidFileTypeException(
                    "File content does not match declared content type");
            }

        } catch (InvalidFileTypeException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidFileTypeException("Failed to validate file content");
        }
    }

    /**
     * Checks if an image exists.
     *
     * @param filename the filename of the image
     * @return true if the image exists, false otherwise
     */
    public boolean imageExists(String filename) {
        return storagePort.exists(filename);
    }

    /**
     * Retrieves an image by filename.
     *
     * @param filename the filename of the image to retrieve
     * @return the image content as InputStream
     * @throws ImageNotFoundException if the image does not exist
     * @throws StorageException if there is an internal storage error
     */
    public InputStream retrieveImage(String filename) {
        logger.debug("Retrieving image: {}", filename);
        return storagePort.retrieve(filename);
    }
}
