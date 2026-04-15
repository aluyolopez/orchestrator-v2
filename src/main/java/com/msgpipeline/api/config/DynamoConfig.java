package com.msgpipeline.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Configuración del cliente DynamoDB para AWS SDK v2.
 *
 * PATRÓN: Factory Method (@Bean) — Spring gestiona el ciclo de vida del DynamoDbClient.
 * PRINCIPIO: Dependency Inversion — el repositorio depende del bean, no de la creación.
 *
 * En Lambda, las credenciales se obtienen automáticamente del rol IAM
 * (msg-pipeline-lambda-role). No se requieren claves de acceso en el código.
 */
@Configuration
@RequiredArgsConstructor
public class DynamoConfig {

    private final AppConfig appConfig;

    /**
     * Crea el cliente DynamoDB con la región configurada.
     * En Lambda, las credenciales se obtienen del rol de ejecución IAM.
     *
     * @return DynamoDbClient listo para usar en los repositorios
     */
    @Bean
    public DynamoDbClient dynamoDbClient() {
        // Region.of(): usa la región configurada en AppConfig (us-east-1 por defecto)
        return DynamoDbClient.builder()
                .region(Region.of(appConfig.getAws().getRegion()))
                .build();
    }
}
