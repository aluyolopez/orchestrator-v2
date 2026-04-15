package com.msgpipeline.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entidad de dominio central del sistema msg-pipeline.
 *
 * CAPA: Core / Entities (Clean Architecture).
 * PRINCIPIO: Single Responsibility — representa unicamente el modelo de mensaje.
 *
 * Sesion 06: se agrego el campo 'createdBy' para soportar @PostAuthorize
 * que verifica que el usuario solo pueda ver sus propios mensajes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private String messageId;
    private String messageType;   // NOTIFICATION | RECORD
    private String channel;       // EMAIL | SMS | PUSH
    private String recipientEmail;
    private String content;
    private MessageStatus status;
    private String createdAt;
    private String updatedAt;

    // Sesion 06: ID del usuario Cognito (sub claim) que creo el mensaje
    // Permite aplicar @PostAuthorize para que cada usuario vea solo sus mensajes
    private String createdBy;

    // Sesion 06: username de Cognito para logs y auditoría
    private String createdByUsername;

    /**
     * Factory Method: crea un mensaje nuevo en estado PENDING.
     * Sin createdBy — se asigna en el servicio cuando el usuario esta autenticado.
     */
    public static Message create(String messageType, String channel,
                                  String recipientEmail, String content) {
        String now = Instant.now().toString();
        return Message.builder()
                .messageType(messageType)
                .channel(channel)
                .recipientEmail(recipientEmail)
                .content(content)
                .status(MessageStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
