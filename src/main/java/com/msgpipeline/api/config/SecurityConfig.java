package com.msgpipeline.api.config;

import com.msgpipeline.api.security.CustomAccessDeniedHandler;
import com.msgpipeline.api.security.CustomAuthenticationEntryPoint;
import com.msgpipeline.api.security.JwtClaimsFilter;
import com.msgpipeline.api.security.MdcLoggingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracion de Spring Security con JWT de Amazon Cognito.
 *
 * NOTA CRITICA para Lambda + SnapStart:
 * La aplicacion usa lazy-initialization=true en application-prod.yml.
 * Esto evita que Spring Security descargue las claves publicas de Cognito
 * durante la fase de inicializacion de Lambda (que causaria NotStabilized).
 * Las claves JWK se cargan en la primera peticion real autenticada.
 *
 * Perfil 'prod':  valida JWT de Cognito (COGNITO_JWK_SET_URI)
 * Perfil 'local': permite todo sin autenticacion
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    /**
     * SecurityFilterChain para perfil 'prod'.
     * Valida JWT de Cognito en cada peticion autenticada.
     */
    @Bean
    @Profile("prod")
    public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new CognitoJwtAuthenticationConverter())
                        )
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                )
                .addFilterBefore(new MdcLoggingFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new JwtClaimsFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * JwtDecoder para perfil 'prod'.
     * Con lazy-initialization=true, este bean se instancia en la primera
     * peticion autenticada, no durante el arranque de Lambda.
     * Esto resuelve el error NotStabilized en publish-version de SAM.
     */
    @Bean
    @Profile("prod")
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    /**
     * SecurityFilterChain para perfil 'local'.
     * Permite todo sin autenticacion — facilita el desarrollo local.
     */
    @Bean
    @Profile("local")
    public SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(new MdcLoggingFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
