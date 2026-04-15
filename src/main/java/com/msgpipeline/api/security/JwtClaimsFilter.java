package com.msgpipeline.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que extrae claims del JWT y los expone como atributos del request.
 *
 * PATRON: Chain of Responsibility (filtro de Spring Security).
 * OncePerRequestFilter: garantiza ejecucion exactamente UNA VEZ por peticion.
 *
 * Los controllers pueden acceder a los claims via:
 *   @AuthenticationPrincipal Jwt jwt
 * O directamente via request.getAttribute("userId").
 *
 * Claims extraidos de Cognito JWT:
 * - sub           → userId (UUID unico del usuario en Cognito)
 * - cognito:username → username
 * - email         → email del usuario
 */
@Slf4j
public class JwtClaimsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();

            // Exponer claims como atributos del request para uso en controllers
            request.setAttribute("userId",   jwt.getSubject());
            request.setAttribute("username", jwt.getClaimAsString("cognito:username"));
            request.setAttribute("email",    jwt.getClaimAsString("email"));

            log.debug("Claims extraidos para usuario: {} [sub={}]",
                    jwt.getClaimAsString("cognito:username"), jwt.getSubject());
        }

        filterChain.doFilter(request, response);
    }
}
