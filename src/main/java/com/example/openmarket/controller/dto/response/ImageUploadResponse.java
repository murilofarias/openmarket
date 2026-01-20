package com.example.openmarket.controller.dto.response;

/**
 * Response containing the URL of the uploaded image.
 */
public record ImageUploadResponse(
    String url,
    String filename
) {}
