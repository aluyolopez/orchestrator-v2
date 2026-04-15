package com.msgpipeline.api.domain.port;

import com.msgpipeline.api.domain.model.Message;

/**
 * Puerto de salida — contrato para publicar mensajes en SQS.
 *
 * PATRÓN: Port (Hexagonal Architecture) — define el contrato sin acoplarse a AWS SQS.
 * PRINCIPIO: Dependency Inversion — el dominio depende de esta abstracción,
 *            no de la implementación concreta de AWS SDK.
 *
 * Implementaciones:
 * - SqsPublisherAdapter (prod): publica en cola SQS real
 * - NoOpSqsPublisher (local): no hace nada — evita llamadas AWS en local
 */
public interface SqsPublisher {

    /**
     * Publica un mensaje en la cola SQS para procesamiento asíncrono.
     *
     * @param message mensaje a encolar
     */
    void publish(Message message);
}
