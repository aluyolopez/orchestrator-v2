package com.msgpipeline.api.domain.service;

import com.msgpipeline.api.domain.model.Message;
import com.msgpipeline.api.domain.model.MessageStatus;
import com.msgpipeline.api.domain.port.MessageRepository;
import com.msgpipeline.api.domain.port.SqsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso principal del msg-pipeline orchestrator.
 *
 * CAPA: Use Cases (Clean Architecture).
 * PRINCIPIO: Dependency Inversion — depende de interfaces (ports), no de AWS SDK.
 *
 * Sesion 06: createMessage ahora acepta userId y username del JWT de Cognito.
 * Esto permite @PostAuthorize en el controller y auditoria por usuario.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final SqsPublisher sqsPublisher;

    /**
     * Crea un mensaje, lo persiste en DynamoDB y lo encola en SQS.
     *
     * @param messageType    tipo (NOTIFICATION, RECORD)
     * @param channel        canal (EMAIL, SMS, PUSH)
     * @param recipientEmail destinatario
     * @param content        contenido del mensaje
     * @param userId         sub (UUID) del usuario Cognito — null en perfil local
     * @param username       cognito:username — null en perfil local
     */
    public Message createMessage(String messageType, String channel,
                                  String recipientEmail, String content,
                                  String userId, String username) {
        String messageId = UUID.randomUUID().toString();

        Message message = Message.create(messageType, channel, recipientEmail, content);
        message.setMessageId(messageId);
        message.setCreatedBy(userId);
        message.setCreatedByUsername(username);

        log.info("Creando mensaje [id={}] [tipo={}] [canal={}] [usuario={}]",
                messageId, messageType, channel, username);

        Message saved = messageRepository.save(message);
        log.info("Mensaje persistido en DynamoDB [id={}] [estado={}]",
                saved.getMessageId(), saved.getStatus());

        sqsPublisher.publish(saved);
        return saved;
    }

    /** Sobrecarga sin autenticacion — compatibilidad con perfil local */
    public Message createMessage(String messageType, String channel,
                                  String recipientEmail, String content) {
        return createMessage(messageType, channel, recipientEmail, content,
                "local-user", "local");
    }

    public Optional<Message> findById(String messageId) {
        log.debug("Buscando mensaje [id={}]", messageId);
        return messageRepository.findById(messageId);
    }

    public List<Message> findAll() {
        return messageRepository.findAll();
    }

    public void deleteMessage(String messageId) {
        log.info("Eliminando mensaje [id={}]", messageId);
        // En esta implementacion simplificada actualizamos el estado a FAILED
        // En produccion se podria agregar un metodo delete al repositorio
        messageRepository.findById(messageId).ifPresentOrElse(
                m -> {
                    m.setStatus(MessageStatus.FAILED);
                    m.setUpdatedAt(Instant.now().toString());
                    messageRepository.update(messageId, m);
                },
                () -> { throw new IllegalArgumentException(
                        "Mensaje no encontrado [id=" + messageId + "]"); }
        );
    }

    public Message updateStatus(String messageId, MessageStatus newStatus) {
        Message existing = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mensaje no encontrado [id=" + messageId + "]"));
        existing.setStatus(newStatus);
        existing.setUpdatedAt(Instant.now().toString());
        return messageRepository.update(messageId, existing);
    }
}
