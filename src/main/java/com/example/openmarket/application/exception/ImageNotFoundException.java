package com.example.openmarket.application.exception;

public class ImageNotFoundException extends RuntimeException {

    private final String filename;

    public ImageNotFoundException(String filename) {
        super("Image not found: " + filename);
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
