package com.msgpipeline.api.adapter.in.rest;

import com.msgpipeline.api.adapter.in.rest.dto.ApiResponse;
import com.msgpipeline.api.adapter.in.rest.dto.CreateMessageRequest;
import com.msgpipeline.api.adapter.in.rest.dto.MessageResponse;
import com.msgpipeline.api.domain.model.Message;
import com.msgpipeline.api.domain.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador primario REST — orquestador de mensajes.
 *
 * CAPA: Interface Adapters (Clean Architecture).
 * PATRON: Adaptador Primario (Hexagonal Architecture).
 *
 * Sesion 05/06: @PreAuthorize y @PostAuthorize para RBAC basado en roles Cognito.
 * - ROLE_USER:  puede crear y ver sus propios mensajes.
 * - ROLE_ADMIN: puede crear, ver todos y eliminar mensajes.
 *
 * En perfil 'local': SecurityConfig permite todo sin token (facilita desarrollo).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Mensajes", description = "Pipeline de mensajeria serverless")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    /**
     * POST /api/v1/messages
     *
     * @PreAuthorize: ROLE_USER o ROLE_ADMIN pueden crear mensajes.
     * @AuthenticationPrincipal: inyecta el JWT del usuario autenticado.
     * En perfil local: jwt es null — el servicio usa "local-user".
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    @Operation(summary = "Crear mensaje",
               description = "Requiere rol USER o ADMIN. Encola el mensaje en SQS.")
    public ResponseEntity<ApiResponse<MessageResponse>> createMessage(
            @Valid @RequestBody CreateMessageRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        // Extraer identidad del usuario autenticado (null en perfil local)
        String userId   = jwt != null ? jwt.getSubject() : null;
        String username = jwt != null ? jwt.getClaimAsString("cognito:username") : "local";

        log.info("POST /api/v1/messages [tipo={}] [canal={}] [usuario={}]",
                request.getMessageType(), request.getChannel(), username);

        Message created = messageService.createMessage(
                request.getMessageType(), request.getChannel(),
                request.getRecipientEmail(), request.getContent(),
                userId, username);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Mensaje creado exitosamente",
                        MessageResponse.from(created)));
    }

    /**
     * GET /api/v1/messages
     *
     * @PreAuthorize: solo ADMIN puede listar todos los mensajes.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Listar mensajes (solo ADMIN)")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getAllMessages() {
        log.info("GET /api/v1/messages [ADMIN]");

        List<MessageResponse> messages = messageService.findAll()
                .stream().map(MessageResponse::from).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok("Mensajes obtenidos", messages));
    }

    /**
     * GET /api/v1/messages/{messageId}
     *
     * @PreAuthorize:  USER o ADMIN pueden consultar.
     * @PostAuthorize: despues de obtener el resultado, verifica que
     *                 el usuario sea el creador O sea ADMIN.
     *                 Evita que USER vea mensajes de otros usuarios.
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostAuthorize("returnObject.body.data.createdBy == authentication.name or hasRole('ADMIN')")
    @GetMapping("/{messageId}")
    @Operation(summary = "Buscar mensaje por ID")
    public ResponseEntity<ApiResponse<MessageResponse>> getMessageById(
            @PathVariable String messageId) {
        log.info("GET /api/v1/messages/{}", messageId);

        return messageService.findById(messageId)
                .map(MessageResponse::from)
                .map(msg -> ResponseEntity.ok(ApiResponse.ok("Mensaje encontrado", msg)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Mensaje no encontrado [id=" + messageId + "]")));
    }

    /**
     * DELETE /api/v1/messages/{messageId}
     *
     * @PreAuthorize: solo ADMIN puede eliminar mensajes.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{messageId}")
    @Operation(summary = "Eliminar mensaje (solo ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable String messageId) {
        log.info("DELETE /api/v1/messages/{} [ADMIN]", messageId);
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok(ApiResponse.ok("Mensaje eliminado"));
    }
}
