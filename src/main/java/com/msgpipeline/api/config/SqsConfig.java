package com.msgpipeline.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * Configuracion de AWS SQS y ObjectMapper compartido.
 *
 * PATRON: Factory Method (@Bean).
 * SqsClient solo se crea en perfil 'prod' — en local no hay credenciales AWS.
 */
@Configuration
@RequiredArgsConstructor
public class SqsConfig {

    private final AppConfig appConfig;

    /**
     * Cliente SQS — solo activo en perfil 'prod' (Lambda en AWS).
     * Las credenciales vienen del rol IAM de ejecucion de Lambda.
     */
    @Bean
    @Profile("prod")
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of(appConfig.getAws().getRegion()))
                .build();
    }

    /**
     * ObjectMapper compartido para serializacion JSON.
     * Disponible en ambos perfiles (local y prod).
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
