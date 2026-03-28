package ru.streamer.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(org.springframework.web.reactive.resource.NoResourceFoundException.class)
    public void handleNoResourceFoundException(org.springframework.web.reactive.resource.NoResourceFoundException ex) {
        // Игнорируем ошибки для статических ресурсов (например, Chrome DevTools)
        String message = ex.getMessage();
        if (message == null || !message.contains(".well-known/")) {
            log.debug("Static resource not found: {}", message);
        }
    }

    @ExceptionHandler(ReadFileSystemException.class)
    public ResponseEntity<Map<String, Object>> handleReadFileSystemException(ReadFileSystemException ex) {
        log.error("File system read error", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ошибка чтения файловой системы",
                ex.getMessage()
        );
    }

    @ExceptionHandler(TranscodingException.class)
    public ResponseEntity<Map<String, Object>> handleTranscodingException(TranscodingException ex) {
        log.error("Transcoding error", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ошибка транскодирования видео",
                ex.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Некорректный запрос",
                ex.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера",
                ex.getMessage()
        );
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String message,
            String details
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("details", details);

        return ResponseEntity.status(status).body(body);
    }
}
