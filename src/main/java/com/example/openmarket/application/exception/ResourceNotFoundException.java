package com.example.openmarket.application.exception;

public class ResourceNotFoundException extends RuntimeException {

    private static final String BASE_MESSAGE = "No %s was found with id: %s";

    public ResourceNotFoundException(String resourceName, String id) {
        super(String.format(BASE_MESSAGE, resourceName, id));
    }
}
