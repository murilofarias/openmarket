package com.example.openmarket.application.port;

import java.io.InputStream;

/**
 * Port for file storage operations.
 * This abstraction allows the application to be independent of the storage mechanism
 * (local filesystem, S3, Azure Blob, etc.)
 */
public interface StoragePort {

    /**
     * Stores a file and returns the generated filename.
     *
     * @param inputStream The file content
     * @param originalFilename The original filename (used for extension detection)
     * @param contentType The MIME type of the file
     * @param fileSize The size of the file in bytes
     * @return The generated filename used to store the file
     */
    String store(InputStream inputStream, String originalFilename,
                 String contentType, long fileSize);

    /**
     * Retrieves a file by its filename.
     *
     * @param filename The stored filename
     * @return The file content as InputStream
     * @throws com.example.openmarket.application.exception.ImageNotFoundException if file does not exist
     * @throws com.example.openmarket.application.exception.StorageException if there is an internal storage error
     */
    InputStream retrieve(String filename);

    /**
     * Deletes a file by its filename.
     *
     * @param filename The stored filename
     */
    void delete(String filename);

    /**
     * Checks if a file exists.
     *
     * @param filename The stored filename
     * @return true if file exists, false otherwise
     */
    boolean exists(String filename);
}
