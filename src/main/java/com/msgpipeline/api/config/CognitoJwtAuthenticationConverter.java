package com.msgpipeline.api.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convierte los claims del JWT de Cognito en roles de Spring Security.
 *
 * PATRON: Converter (Strategy) — transforma el JWT en un token de autenticacion.
 * PRINCIPIO: Single Responsibility — solo mapea grupos Cognito a roles Spring.
 *
 * Cognito incluye los grupos del usuario en el claim "cognito:groups".
 * Mapeo:
 *   msg-pipeline-admin → ROLE_ADMIN
 *   msg-pipeline-user  → ROLE_USER
 *
 * Si el usuario no tiene grupos → se asigna ROLE_USER por defecto.
 */
public class CognitoJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        // El nombre principal del token es el cognito:username
        return new JwtAuthenticationToken(jwt, authorities,
                jwt.getClaimAsString("cognito:username"));
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> groups = jwt.getClaimAsStringList("cognito:groups");

        if (groups == null || groups.isEmpty()) {
            // Usuario sin grupos asignados: rol mínimo USER
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // Mapear: "msg-pipeline-admin" → "ROLE_ADMIN"
        //         "msg-pipeline-user"  → "ROLE_USER"
        return groups.stream()
                .map(g -> g.replace("msg-pipeline-", "").toUpperCase())
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
