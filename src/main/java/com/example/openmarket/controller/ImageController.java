package com.example.openmarket.controller;

import com.example.openmarket.application.exception.ImageNotFoundException;
import com.example.openmarket.application.service.ImageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Retrieve an image by filename (PUBLIC)
     * GET /images/{filename}
     */
    @GetMapping("/{filename}")
    public ResponseEntity<StreamingResponseBody> getImage(@PathVariable String filename) {
        if (!imageService.imageExists(filename)) {
            throw new ImageNotFoundException(filename);
        }

        String contentType = getContentType(filename);

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = imageService.retrieveImage(filename)) {
                inputStream.transferTo(outputStream);
            }
        };

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
            .body(responseBody);
    }

    private String getContentType(String filename) {
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".png")) return "image/png";
        if (lowerFilename.endsWith(".gif")) return "image/gif";
        if (lowerFilename.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}
