package com.msgpipeline.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Clase principal de la aplicación msg-pipeline-orchestrator.
 *
 * PATRÓN: Entry Point (Frameworks & Drivers Layer — Clean Architecture)
 * - Arranca el contexto Spring Boot.
 * - Se usa también como referencia en LambdaHandler para inicializar el contenedor.
 *
 * IMPORTANTE Spring Boot 3.x + Lambda:
 * - Se usa SpringApplicationBuilder con WebApplicationType.SERVLET para forzar
 *   el contexto servlet. Sin esto, Spring Boot 3.x puede fallar en Lambda
 *   al no detectar un servidor embebido activo.
 */
@SpringBootApplication
public class MsgPipelineApiApplication {

    public static void main(String[] args) {
        // SpringApplicationBuilder.web(SERVLET): fuerza contexto servlet en Spring Boot 3.x.
        // Crítico para que aws-serverless-java-container funcione correctamente en Lambda.
        new SpringApplicationBuilder(MsgPipelineApiApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
