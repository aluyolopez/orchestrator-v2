package com.msgpipeline.api.domain.port;

import com.msgpipeline.api.domain.model.Message;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida — contrato para persistencia de mensajes.
 *
 * PATRÓN: Repository (Domain-Driven Design).
 * PRINCIPIO: Dependency Inversion — el dominio define la interfaz,
 *            el adaptador (DynamoDB) la implementa.
 *
 * El núcleo de negocio NUNCA depende de DynamoDB directamente.
 * Puede cambiarse por RDS o cualquier otra base de datos sin tocar el dominio.
 */
public interface MessageRepository {

    /**
     * Guarda un nuevo mensaje en el repositorio.
     * @param message mensaje a persistir
     * @return mensaje guardado con messageId asignado
     */
    Message save(Message message);

    /**
     * Busca un mensaje por su ID único.
     * @param messageId identificador del mensaje
     * @return Optional con el mensaje si existe
     */
    Optional<Message> findById(String messageId);

    /**
     * Retorna todos los mensajes almacenados.
     * @return lista de mensajes (puede estar vacía)
     */
    List<Message> findAll();

    /**
     * Actualiza el estado de un mensaje existente.
     * @param messageId identificador del mensaje
     * @param message   datos actualizados
     * @return mensaje actualizado
     */
    Message update(String messageId, Message message);
}
