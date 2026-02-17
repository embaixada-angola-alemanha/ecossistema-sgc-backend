package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.sgc.dto.NotificationLogResponse;
import ao.gov.embaixada.sgc.dto.NotificationPreferenceResponse;
import ao.gov.embaixada.sgc.dto.NotificationPreferenceUpdateRequest;
import ao.gov.embaixada.sgc.service.NotificationLogService;
import ao.gov.embaixada.sgc.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notificacoes")
@Tag(name = "Notificacoes", description = "Gestao de preferencias e historico de notificacoes")
public class NotificacaoController {

    private final NotificationPreferenceService preferenceService;
    private final NotificationLogService logService;

    public NotificacaoController(NotificationPreferenceService preferenceService,
                                  NotificationLogService logService) {
        this.preferenceService = preferenceService;
        this.logService = logService;
    }

    @GetMapping("/preferencias/{cidadaoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Obter preferencias de notificacao de um cidadao")
    public ResponseEntity<ApiResponse<List<NotificationPreferenceResponse>>> getPreferences(
            @PathVariable UUID cidadaoId) {
        List<NotificationPreferenceResponse> preferences = preferenceService.findByCidadaoId(cidadaoId);
        return ResponseEntity.ok(ApiResponse.success("Preferencias obtidas", preferences));
    }

    @PutMapping("/preferencias/{cidadaoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Actualizar preferencias de notificacao de um cidadao")
    public ResponseEntity<ApiResponse<List<NotificationPreferenceResponse>>> updatePreferences(
            @PathVariable UUID cidadaoId,
            @Valid @RequestBody NotificationPreferenceUpdateRequest request) {
        List<NotificationPreferenceResponse> preferences = preferenceService.updatePreferences(cidadaoId, request);
        return ResponseEntity.ok(ApiResponse.success("Preferencias actualizadas", preferences));
    }

    @GetMapping("/historico/{cidadaoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Obter historico de notificacoes de um cidadao")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationLogResponse>>> getHistoricoByCidadao(
            @PathVariable UUID cidadaoId,
            @PageableDefault(size = 20) Pageable pageable) {
        var page = logService.findByCidadaoId(cidadaoId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Historico obtido", PagedResponse.of(page)));
    }

    @GetMapping("/historico")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL')")
    @Operation(summary = "Obter historico de todas as notificacoes")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationLogResponse>>> getAllHistorico(
            @PageableDefault(size = 20) Pageable pageable) {
        var page = logService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Historico obtido", PagedResponse.of(page)));
    }
}
