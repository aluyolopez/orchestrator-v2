package com.msgpipeline.api.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Wrapper genérico para todas las respuestas de la API REST.
 *
 * PATRÓN: Template Method — estructura consistente para todas las respuestas.
 * PRINCIPIO: Open/Closed — extensible sin modificar los controllers.
 *
 * Todas las respuestas del sistema siguen este formato:
 * {
 *   "success": true,
 *   "message": "Mensaje creado exitosamente",
 *   "data": { ... },
 *   "timestamp": "2024-01-15T10:30:00Z"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta estándar de la API msg-pipeline")
public class ApiResponse<T> {

    @Schema(description = "Indica si la operación fue exitosa", example = "true")
    private boolean success;

    @Schema(description = "Mensaje descriptivo del resultado", example = "Mensaje creado exitosamente")
    private String message;

    @Schema(description = "Datos retornados por la operación")
    private T data;

    @Schema(description = "Timestamp de la respuesta", example = "2024-01-15T10:30:00Z")
    private String timestamp;

    // ─── Factory methods ─────────────────────────────────────────────────────

    /** Respuesta exitosa con datos */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
    }

    /** Respuesta exitosa sin datos */
    public static <T> ApiResponse<T> ok(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now().toString())
                .build();
    }

    /** Respuesta de error */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now().toString())
                .build();
    }
}
