package com.msgpipeline.api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

/**
 * Indicador de salud personalizado para verificar conectividad con AWS DynamoDB.
 *
 * PATRÓN: Strategy (HealthIndicator es una interfaz que implementamos).
 * Acceso: GET /actuator/health
 *
 * En perfil 'local': puede fallar si no hay credenciales AWS configuradas.
 * En perfil 'prod' (Lambda): siempre disponible porque usa el rol IAM.
 */
@Slf4j
@Component("awsConnectivity")
@RequiredArgsConstructor
public class AwsHealthIndicator implements HealthIndicator {

    private final DynamoDbClient dynamoDbClient;

    @Override
    public Health health() {
        try {
            // Verificar conectividad listando las tablas DynamoDB (operación de bajo costo)
            ListTablesResponse response = dynamoDbClient.listTables();
            return Health.up()
                    .withDetail("dynamo_tables", response.tableNames().size())
                    .withDetail("region", "us-east-1")
                    .build();
        } catch (Exception e) {
            log.warn("AWS DynamoDB no disponible: {}", e.getMessage());
            // En entorno local sin credenciales, devolvemos DOWN pero no bloqueamos el servicio
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("hint", "Configura credenciales AWS o usa perfil 'local'")
                    .build();
        }
    }
}
