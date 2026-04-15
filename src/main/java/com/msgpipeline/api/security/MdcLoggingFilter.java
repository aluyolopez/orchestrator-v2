package com.msgpipeline.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro MDC (Mapped Diagnostic Context) para correlacion de logs.
 *
 * PATRON: Chain of Responsibility (filtro de Spring).
 * PRINCIPIO: Single Responsibility — solo agrega correlationId a los logs.
 *
 * Beneficio (Session 07): permite correlacionar todos los logs de una misma
 * peticion en CloudWatch Logs Insights con la query:
 *   fields @timestamp, correlationId, @message
 *   | filter correlationId = "abc-123"
 *   | sort @timestamp asc
 *
 * CRITICO: siempre limpiar el MDC en el bloque finally para evitar leaks
 * en el pool de threads de Lambda (warm invocations).
 */
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Usar el header de correlacion si viene del cliente, o generar uno nuevo
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString().substring(0, 8);
        }

        // Agregar al MDC — Logstash los incluye automaticamente en cada log JSON
        MDC.put("correlationId", correlationId);
        MDC.put("httpMethod",    request.getMethod());
        MDC.put("requestPath",   request.getRequestURI());

        // Propagar el correlationId en la respuesta para trazabilidad cliente-servidor
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // CRITICO: limpiar MDC siempre — evita que datos de una peticion
            // contaminen los logs de la siguiente en warm invocations
            MDC.clear();
        }
    }
}
