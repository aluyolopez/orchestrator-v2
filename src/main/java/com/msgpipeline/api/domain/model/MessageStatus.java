package com.msgpipeline.api.domain.model;

/**
 * Estados del ciclo de vida de un mensaje en el pipeline.
 *
 * CAPA: Core / Entities (Clean Architecture).
 *
 * Flujo del pipeline:
 * PENDING → VALIDATING → VALIDATED → PROCESSING → PROCESSED → NOTIFIED
 *                     ↘ FAILED (en cualquier etapa)
 */
public enum MessageStatus {

    /** Mensaje recibido por el orchestrator, aún no enviado al pipeline */
    PENDING,

    /** El validator está revisando el mensaje */
    VALIDATING,

    /** Validación exitosa — listo para ser procesado */
    VALIDATED,

    /** El processor está guardando el mensaje en DynamoDB */
    PROCESSING,

    /** Guardado exitosamente en DynamoDB */
    PROCESSED,

    /** SNS/Lambda envió la notificación al destinatario */
    NOTIFIED,

    /** Error en alguna etapa del pipeline */
    FAILED
}
