package com.msgpipeline.api.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para crear un mensaje.
 *
 * PATRON: DTO — separa la API del modelo de dominio.
 * PRINCIPIO: Single Responsibility — solo transporta y valida datos de entrada.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para crear un nuevo mensaje en el pipeline")
public class CreateMessageRequest {

    @NotBlank(message = "El tipo de mensaje es obligatorio")
    @Pattern(regexp = "NOTIFICATION|RECORD",
            message = "El tipo de mensaje debe ser NOTIFICATION o RECORD")
    @Schema(description = "Tipo de mensaje", example = "NOTIFICATION",
            allowableValues = {"NOTIFICATION", "RECORD"})
    private String messageType;

    @NotBlank(message = "El canal es obligatorio")
    @Pattern(regexp = "EMAIL|SMS|PUSH",
            message = "El canal debe ser EMAIL, SMS o PUSH")
    @Schema(description = "Canal de envio", example = "EMAIL",
            allowableValues = {"EMAIL", "SMS", "PUSH"})
    private String channel;

    @NotBlank(message = "El email del destinatario es obligatorio")
    @Email(message = "El email del destinatario no tiene formato valido")
    @Schema(description = "Email del destinatario", example = "cliente@ejemplo.com")
    private String recipientEmail;

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Size(min = 10, max = 1000,
            message = "El contenido debe tener entre 10 y 1000 caracteres")
    @Schema(description = "Contenido del mensaje",
            example = "Su pedido ha sido confirmado exitosamente.")
    private String content;
}
