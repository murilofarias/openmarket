package com.example.openmarket.controller.exceptionHandler;

import com.example.openmarket.application.exception.DomainException;
import com.example.openmarket.application.exception.FileSizeExceededException;
import com.example.openmarket.application.exception.ImageNotFoundException;
import com.example.openmarket.application.exception.InvalidFileTypeException;
import com.example.openmarket.application.exception.ResourceNotFoundException;
import com.example.openmarket.application.exception.StorageException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Log validation errors at WARN level (expected business errors)
        logger.warn("Validation failed for request {} {} - User: {} - Errors: {}",
                request.getMethod(),
                request.getRequestURI(),
                getCurrentUserId(),
                errors);

        ValidationErrorResponse response = new ValidationErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<StandardErrorResponse> handleResourceNotFound(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Log validation errors at WARN level (expected business errors)
        logger.warn("Resource not found for request {} {} - User: {}",
                request.getMethod(),
                request.getRequestURI(),
                getCurrentUserId());

        StandardErrorResponse response = new StandardErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<StandardErrorResponse> handleDomainErrors(
            DomainException ex,
            HttpServletRequest request) {

        // Log domain errors at WARN level (expected business rule violations)
        logger.warn("Domain exception for request {} {} - User: {} - Message: {}",
                request.getMethod(),
                request.getRequestURI(),
                getCurrentUserId(),
                ex.getMessage());

        StandardErrorResponse response = new StandardErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        // Log resource not found at INFO level (common occurrence, not an error)
        logger.info("Resource not found for request {} {} - User: {} - Message: {}",
                request.getMethod(),
                request.getRequestURI(),
                getCurrentUserId(),
                ex.getMessage());

        StandardErrorResponse response = new StandardErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<StandardErrorResponse> handleImageNotFound(
            ImageNotFoundException ex,
            HttpServletRequest request) {

        logger.info("Image not found for request {} {} - User: {} - Filename: {}",
                request.getMethod(),
                request.getRequestURI(),
                getCurrentUserId(),
                ex.getFilename());

        StandardErrorResponse response = new StandardErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<StandardErrorResponse> handleStorageErrors(
            StorageException ex,
            HttpServletRequest request) {

        logger.error("Storage exception for request {} {} - User: {} - Message: {}",
                request.getMethod(),
                request.getRequestURI(),
                getCurrentUserId(),
                ex.getMessage(),
                ex);

        StandardErrorResponse response = new StandardErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to process file. Please try again later."
        );

        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<StandardErrorResponse> handleInvalidFileType(
            InvalidFileTypeException ex,
            HttpServletRequest request) {

        logger.warn("Invalid file type for request {} {} - User: {} - Message: {}",
                request.getMethod(),
                request.getRequestURI(),
                getCurrentUserId(),
                ex.getMessage());

        StandardErrorResponse response = new StandardErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<StandardErrorResponse> handleFileSizeExceeded(
            FileSizeExceededException ex,
            HttpServletRequest request) {

        logger.warn("File size exceeded for request {} {} - User: {} - Message: {}",
                request.getMethod(),
                request.getRequestURI(),
                getCurrentUserId(),
                ex.getMessage());

        StandardErrorResponse response = new StandardErrorResponse(
                LocalDateTime.now(),
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<StandardErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {

        logger.warn("Max upload size exceeded for request {} {} - User: {}",
                request.getMethod(),
                request.getRequestURI(),
                getCurrentUserId());

        StandardErrorResponse response = new StandardErrorResponse(
                LocalDateTime.now(),
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "File size exceeds maximum allowed size of 10 MB"
        );

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardErrorResponse> handleGenericErrors(
            Exception ex,
            HttpServletRequest request) {

        // Log unexpected errors at ERROR level with full stack trace
        logger.error("Unexpected error for request {} {} - User: {} - Error: {}",
                request.getMethod(),
                request.getRequestURI(),
                getCurrentUserId(),
                ex.getMessage(),
                ex);  // This includes the full stack trace

        // Don't expose internal error details to the client
        StandardErrorResponse response = new StandardErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Please try again later."
        );

        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * Helper method to extract current user ID from security context.
     * Returns "anonymous" if user is not authenticated.
     */
    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !authentication.getPrincipal().equals("anonymousUser")) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Ignore errors when extracting user ID
        }
        return "anonymous";
    }

    public record StandardErrorResponse(
            LocalDateTime timestamp,
            int status,
            String message) {}

    public record ValidationErrorResponse(
            LocalDateTime timestamp,
            int status,
            String message,
            Map<String, String> errors
    ) {}
}
