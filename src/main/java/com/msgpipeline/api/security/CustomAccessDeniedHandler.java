package com.msgpipeline.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Handler para errores HTTP 403 Forbidden.
 *
 * 403: el usuario ESTA autenticado pero NO tiene el rol requerido.
 * Se dispara cuando @PreAuthorize rechaza el acceso.
 *
 * PRINCIPIO: Single Responsibility — solo maneja el error 403.
 */
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.warn("403 Forbidden — path={} user={}",
                request.getRequestURI(),
                request.getUserPrincipal() != null
                        ? request.getUserPrincipal().getName() : "unknown");

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = Map.of(
                "success",   false,
                "message",   "Acceso denegado. No tienes los permisos necesarios para esta operacion.",
                "error",     "FORBIDDEN",
                "path",      request.getRequestURI(),
                "timestamp", Instant.now().toString()
        );
        objectMapper.writeValue(response.getWriter(), body);
    }
}
