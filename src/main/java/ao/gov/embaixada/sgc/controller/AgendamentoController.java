package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import ao.gov.embaixada.sgc.enums.TipoAgendamento;
import ao.gov.embaixada.sgc.service.AgendamentoService;
import ao.gov.embaixada.sgc.service.AgendamentoSlotConfig;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final AgendamentoSlotConfig slotConfig;

    public AgendamentoController(AgendamentoService agendamentoService,
                                  AgendamentoSlotConfig slotConfig) {
        this.agendamentoService = agendamentoService;
        this.slotConfig = slotConfig;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    public ResponseEntity<ApiResponse<AgendamentoResponse>> create(
            @Valid @RequestBody AgendamentoCreateRequest request) {
        AgendamentoResponse response = agendamentoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Agendamento criado", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    public ResponseEntity<ApiResponse<AgendamentoResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(agendamentoService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    public ResponseEntity<ApiResponse<PagedResponse<AgendamentoResponse>>> findAll(
            @RequestParam(required = false) UUID cidadaoId,
            @RequestParam(required = false) EstadoAgendamento estado,
            @RequestParam(required = false) TipoAgendamento tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @PageableDefault(size = 20) Pageable pageable) {
        if (cidadaoId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(agendamentoService.findByCidadaoId(cidadaoId, pageable))));
        }
        if (estado != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(agendamentoService.findByEstado(estado, pageable))));
        }
        if (tipo != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(agendamentoService.findByTipo(tipo, pageable))));
        }
        if (dataInicio != null && dataFim != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    PagedResponse.of(agendamentoService.findByDateRange(dataInicio, dataFim, pageable))));
        }
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(agendamentoService.findAll(pageable))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    public ResponseEntity<ApiResponse<AgendamentoResponse>> reschedule(
            @PathVariable UUID id, @Valid @RequestBody AgendamentoUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Agendamento reagendado", agendamentoService.reschedule(id, request)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    public ResponseEntity<ApiResponse<AgendamentoResponse>> updateEstado(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        EstadoAgendamento estado = EstadoAgendamento.valueOf(body.get("estado"));
        String comentario = body.getOrDefault("comentario", null);
        return ResponseEntity.ok(ApiResponse.success(
                agendamentoService.updateEstado(id, estado, comentario)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        agendamentoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    public ResponseEntity<ApiResponse<PagedResponse<AgendamentoHistoricoResponse>>> findHistorico(
            @PathVariable UUID id, @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(agendamentoService.findHistorico(id, pageable))));
    }

    @GetMapping("/slots")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','VIEWER')")
    public ResponseEntity<ApiResponse<List<SlotDisponivelResponse>>> getAvailableSlots(
            @RequestParam TipoAgendamento tipo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(ApiResponse.success(slotConfig.getAvailableSlots(data, tipo)));
    }
}
