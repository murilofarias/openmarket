package com.example.openmarket.application.exception;

/**
 * Exception thrown when file size exceeds the limit.
 */
public class FileSizeExceededException extends DomainException {

    public FileSizeExceededException(String message) {
        super(message);
    }
}
