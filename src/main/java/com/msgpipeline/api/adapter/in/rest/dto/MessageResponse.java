package com.msgpipeline.api.adapter.in.rest.dto;

import com.msgpipeline.api.domain.model.Message;
import com.msgpipeline.api.domain.model.MessageStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de salida que representa un mensaje en la respuesta de la API.
 *
 * PATRON: DTO — expone solo los campos necesarios para el cliente.
 * Sesion 06: incluye createdBy para soportar @PostAuthorize en el controller.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representacion de un mensaje en el pipeline")
public class MessageResponse {

    @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
    private String messageId;

    @Schema(example = "NOTIFICATION")
    private String messageType;

    @Schema(example = "EMAIL")
    private String channel;

    @Schema(example = "cliente@ejemplo.com")
    private String recipientEmail;

    @Schema(example = "Su pedido ha sido confirmado.")
    private String content;

    @Schema(example = "PENDING")
    private MessageStatus status;

    @Schema(example = "2024-01-15T10:30:00Z")
    private String createdAt;

    @Schema(example = "2024-01-15T10:30:05Z")
    private String updatedAt;

    // Sesion 06: sub del usuario Cognito — usado por @PostAuthorize
    @Schema(example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String createdBy;

    @Schema(example = "estudiante01")
    private String createdByUsername;

    public static MessageResponse from(Message message) {
        return MessageResponse.builder()
                .messageId(message.getMessageId())
                .messageType(message.getMessageType())
                .channel(message.getChannel())
                .recipientEmail(message.getRecipientEmail())
                .content(message.getContent())
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .createdBy(message.getCreatedBy())
                .createdByUsername(message.getCreatedByUsername())
                .build();
    }
}
