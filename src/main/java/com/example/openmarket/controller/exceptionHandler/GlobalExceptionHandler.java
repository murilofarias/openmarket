package com.example.openmarket.controller.exceptionHandler;

import com.example.openmarket.application.exception.DomainException;
import com.example.openmarket.application.exception.ResourceNotFoundException;
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
