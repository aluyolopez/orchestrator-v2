package com.msgpipeline.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Handler para errores HTTP 401 Unauthorized.
 *
 * 401: el usuario NO esta autenticado (sin token o token invalido).
 * 403: el usuario esta autenticado pero SIN permisos (ver CustomAccessDeniedHandler).
 *
 * PRINCIPIO: Single Responsibility — solo maneja el error 401.
 */
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("401 Unauthorized — path={} error={}",
                request.getRequestURI(), authException.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = Map.of(
                "success",   false,
                "message",   "No autenticado. Se requiere un token JWT valido de Cognito.",
                "error",     "UNAUTHORIZED",
                "path",      request.getRequestURI(),
                "timestamp", Instant.now().toString()
        );
        objectMapper.writeValue(response.getWriter(), body);
    }
}
