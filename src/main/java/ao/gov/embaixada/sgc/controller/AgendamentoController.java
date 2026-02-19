package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import ao.gov.embaixada.sgc.enums.TipoAgendamento;
import ao.gov.embaixada.sgc.service.AgendamentoService;
import ao.gov.embaixada.sgc.service.AgendamentoSlotConfig;
import ao.gov.embaixada.sgc.service.CitizenContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agendamentos")
@Tag(name = "Agendamentos", description = "Sistema de agendamento consular")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final AgendamentoSlotConfig slotConfig;
    private final CitizenContextService citizenContext;

    public AgendamentoController(AgendamentoService agendamentoService,
                                  AgendamentoSlotConfig slotConfig,
                                  CitizenContextService citizenContext) {
        this.agendamentoService = agendamentoService;
        this.slotConfig = slotConfig;
        this.citizenContext = citizenContext;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN')")
    @Operation(summary = "Criar agendamento", description = "Cria um novo agendamento consular verificando disponibilidade de slots")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Agendamento criado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cidadao nao encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflito de horario")
    })
    public ResponseEntity<ApiResponse<AgendamentoResponse>> create(
            @Valid @RequestBody AgendamentoCreateRequest request) {
        if (citizenContext.isCitizenOnly()) {
            UUID ownId = citizenContext.requireCurrentCidadaoId();
            request = new AgendamentoCreateRequest(ownId, request.tipo(), request.dataHora(), request.notas());
        }
        AgendamentoResponse response = agendamentoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Agendamento criado", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Obter agendamento por ID", description = "Retorna os dados de um agendamento especifico")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Agendamento encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Agendamento nao encontrado")
    })
    public ResponseEntity<ApiResponse<AgendamentoResponse>> findById(@PathVariable UUID id) {
        AgendamentoResponse response = agendamentoService.findById(id);
        if (citizenContext.isCitizenOnly() && !citizenContext.canAccessCidadaoData(response.cidadaoId())) {
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Listar agendamentos", description = "Lista agendamentos com filtros opcionais por cidadao, estado, tipo e intervalo de datas")
    public ResponseEntity<ApiResponse<PagedResponse<AgendamentoResponse>>> findAll(
            @RequestParam(required = false) UUID cidadaoId,
            @RequestParam(required = false) EstadoAgendamento estado,
            @RequestParam(required = false) TipoAgendamento tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @PageableDefault(size = 20) Pageable pageable) {
        if (citizenContext.isCitizenOnly()) {
            cidadaoId = citizenContext.requireCurrentCidadaoId();
        }
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
    @Operation(summary = "Reagendar agendamento", description = "Altera a data/hora de um agendamento existente")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Agendamento reagendado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Agendamento nao encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflito de horario")
    })
    public ResponseEntity<ApiResponse<AgendamentoResponse>> reschedule(
            @PathVariable UUID id, @Valid @RequestBody AgendamentoUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Agendamento reagendado", agendamentoService.reschedule(id, request)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER')")
    @Operation(summary = "Alterar estado do agendamento", description = "Transita o agendamento para um novo estado seguindo a maquina de estados")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado alterado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Transicao de estado invalida")
    })
    public ResponseEntity<ApiResponse<AgendamentoResponse>> updateEstado(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        EstadoAgendamento estado = EstadoAgendamento.valueOf(body.get("estado"));
        String comentario = body.getOrDefault("comentario", null);
        return ResponseEntity.ok(ApiResponse.success(
                agendamentoService.updateEstado(id, estado, comentario)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar agendamento", description = "Remove permanentemente um agendamento")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Agendamento eliminado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Agendamento nao encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        agendamentoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Historico do agendamento", description = "Retorna o historico de transicoes de estado do agendamento")
    public ResponseEntity<ApiResponse<PagedResponse<AgendamentoHistoricoResponse>>> findHistorico(
            @PathVariable UUID id, @PageableDefault(size = 50) Pageable pageable) {
        if (citizenContext.isCitizenOnly()) {
            AgendamentoResponse agendamento = agendamentoService.findById(id);
            if (!citizenContext.canAccessCidadaoData(agendamento.cidadaoId())) {
                throw new AccessDeniedException("Access denied");
            }
        }
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(agendamentoService.findHistorico(id, pageable))));
    }

    @GetMapping("/slots")
    @PreAuthorize("hasAnyRole('ADMIN','CONSUL','OFFICER','CITIZEN','VIEWER')")
    @Operation(summary = "Consultar slots disponiveis", description = "Retorna os horarios disponiveis para agendamento numa data e tipo especificos")
    public ResponseEntity<ApiResponse<List<SlotDisponivelResponse>>> getAvailableSlots(
            @RequestParam TipoAgendamento tipo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(ApiResponse.success(slotConfig.getAvailableSlots(data, tipo)));
    }
}
