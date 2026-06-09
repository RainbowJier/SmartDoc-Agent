package com.smartdoc.api;

import com.smartdoc.common.entity.AjaxResult;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Global exception handler — ensures all API errors return a consistent
 * {@link AjaxResult} structure instead of Spring's default error page.
 *
 * <p>Without this, a validation failure would return a 400 with a different JSON
 * schema than a 500, forcing the front-end to handle two different shapes.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle @Valid / @RequestBody validation failures.
     * Example: ChatReq.message is blank.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        log.warn("Validation error: {}", message);
        return AjaxResult.validateFailed(message);
    }

    /**
     * Handle @Validated constraint violations on path/query params.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public AjaxResult<Void> handleConstraint(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return AjaxResult.validateFailed(ex.getMessage());
    }

    /**
     * Handle file size exceeded (spring.servlet.multipart.max-file-size).
     */
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public AjaxResult<Void> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("Upload file too large: {}", ex.getMessage());
        return AjaxResult.failed("File size exceeds the maximum allowed limit (50 MB)");
    }

    /**
     * Catch-all handler — prevents leaking internal exception details.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public AjaxResult<Void> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return AjaxResult.failed("An unexpected error occurred. Please try again later.");
    }
}
