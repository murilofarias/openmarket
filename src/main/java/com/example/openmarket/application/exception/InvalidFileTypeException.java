package com.example.openmarket.application.exception;

/**
 * Exception thrown when an invalid file type is uploaded.
 */
public class InvalidFileTypeException extends DomainException {

    public InvalidFileTypeException(String message) {
        super(message);
    }
}
