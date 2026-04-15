package com.msgpipeline.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI 3.0 para documentación automática de la API.
 *
 * PATRÓN: Builder (OpenAPI DSL).
 * Acceso: http://localhost:8080/swagger-ui.html (perfil local)
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI msgPipelineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("msg-pipeline API")
                        .description("API REST profesional del proyecto msg-pipeline. " +
                                "Orquesta el pipeline de mensajería serverless en AWS.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Anku Academy")
                                .email("soporte@ankuacademy.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Servidor local"),
                        new Server().url("https://{api-id}.execute-api.us-east-1.amazonaws.com/prod")
                                .description("AWS API Gateway (producción)")
                ));
    }
}
