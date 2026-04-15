package com.msgpipeline.api;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * Handler principal de AWS Lambda para msg-pipeline-orchestrator.
 *
 * PATRÓN: Adaptador Primario (Hexagonal Architecture — Interface Adapter Layer)
 * - Recibe eventos HTTP de API Gateway.
 * - Delega el procesamiento al contenedor Spring Boot a través del proxy.
 *
 * REGLAS CLAVE de AWS Lambda con Spring Boot:
 * 1. NO usar @Component — AWS instancia esta clase directamente con reflexión.
 * 2. El campo handler debe ser 'static' para reutilizarse entre invocaciones warm.
 * 3. El bloque 'static {}' se ejecuta UNA SOLA VEZ durante el cold start.
 * 4. Constructor público sin argumentos OBLIGATORIO para que AWS pueda instanciar.
 *
 * Handler AWS: com.msgpipeline.api.LambdaHandler::handleRequest
 */
public class LambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {

    // static: el handler se inicializa UNA SOLA VEZ durante el cold start.
    // Las invocaciones warm reutilizan este contexto Spring ya inicializado.
    // Este es el patrón clave para optimizar el rendimiento en Lambda.
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    // Bloque static: se ejecuta una sola vez al cargar la clase (cold start).
    // Aquí arrancamos el ApplicationContext de Spring Boot.
    static {
        try {
            handler = SpringBootLambdaContainerHandler
                    .getAwsProxyHandler(MsgPipelineApiApplication.class);
        } catch (ContainerInitializationException e) {
            // Si Spring no puede arrancar, la función Lambda no puede inicializarse.
            throw new RuntimeException("Error al inicializar Spring Boot en Lambda", e);
        }
    }

    // Constructor público sin argumentos: OBLIGATORIO para que AWS instancie el handler.
    // AWS usa reflexión para crear la instancia — necesita este constructor.
    public LambdaHandler() {
        // No es necesario inicializar nada aquí — el bloque static ya lo hizo.
    }

    /**
     * Método invocado por AWS Lambda en cada petición.
     *
     * @param event   objeto con headers, path, queryParams y body de la petición HTTP
     * @param context información del contexto Lambda (requestId, tiempo restante, etc.)
     * @return respuesta HTTP que Lambda devuelve a API Gateway
     */
    @Override
    public AwsProxyResponse handleRequest(AwsProxyRequest event, Context context) {
        // Delegar el procesamiento al handler de Spring Boot.
        // Spring MVC maneja el routing, validaciones y la respuesta.
        return handler.proxy(event, context);
    }
}
