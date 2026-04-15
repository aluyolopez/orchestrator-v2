package com.msgpipeline.api.adapter.in.rest;

import com.msgpipeline.api.adapter.in.rest.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador centralizado de excepciones para todos los controllers.
 *
 * PATRÓN: Interceptor / Chain of Responsibility.
 *
 * @RestControllerAdvice: intercepta excepciones de todos los @RestController.
 * Evita duplicar try/catch en cada controller.
 *
 * PRINCIPIO: Single Responsibility — separa el manejo de errores de la lógica de negocio.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación Bean Validation (@Valid).
     * Se dispara cuando un campo del DTO viola una restricción (@NotBlank, @Email, etc.)
     *
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        // Recolectar todos los errores de validación del DTO
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Error de validación en la petición: {}", errors);

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Los datos de entrada no son válidos")
                .data(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja argumentos ilegales (ej: ID de mensaje no encontrado).
     * HTTP 404 Not Found
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento ilegal: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Maneja cualquier excepción no capturada por los handlers anteriores.
     * HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error interno del servidor. Por favor intente más tarde."));
    }
}
