package com.msgpipeline.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración externalizada del proyecto msg-pipeline.
 *
 * PATRÓN: Singleton gestionado por Spring (@Component).
 * PRINCIPIO: Open/Closed — agregar propiedades sin modificar consumidores.
 *
 * Las propiedades se mapean desde application.yml / variables de entorno AWS.
 * En Lambda, las variables de entorno sobreescriben los valores del YAML.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private Aws aws = new Aws();

    @Data
    public static class Aws {
        // Nombre de la tabla DynamoDB — en Lambda se lee desde variable de entorno
        private String dynamoTableName = "msg-pipeline-messages";

        // URL de la cola SQS — en Lambda se lee desde variable de entorno
        private String sqsQueueUrl = "";

        // Región AWS
        private String region = "us-east-1";
    }
}
