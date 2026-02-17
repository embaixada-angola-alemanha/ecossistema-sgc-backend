package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.AgendamentoCreateRequest;
import ao.gov.embaixada.sgc.dto.AgendamentoResponse;
import ao.gov.embaixada.sgc.dto.AgendamentoUpdateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import ao.gov.embaixada.sgc.enums.TipoAgendamento;
import ao.gov.embaixada.sgc.exception.ConflictingAppointmentException;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AgendamentoServiceTest {

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private CidadaoService cidadaoService;

    private UUID cidadaoId;

    @BeforeEach
    void setUp() {
        CidadaoCreateRequest cidadaoReq = new CidadaoCreateRequest(
                "AGD-TEST-" + System.nanoTime(), "Test Cidadao Agendamento", null,
                null, "Angolana", null,
                "test@embaixada.de", null, null, null);
        CidadaoResponse cidadao = cidadaoService.create(cidadaoReq);
        cidadaoId = cidadao.id();
    }

    private AgendamentoCreateRequest passaporteRequest() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        return new AgendamentoCreateRequest(
                cidadaoId, TipoAgendamento.PASSAPORTE,
                nextMonday.atTime(9, 0), "Renovacao de passaporte");
    }

    @Test
    void shouldCreateAgendamento() {
        AgendamentoResponse response = agendamentoService.create(passaporteRequest());

        assertNotNull(response.id());
        assertNotNull(response.numeroAgendamento());
        assertTrue(response.numeroAgendamento().startsWith("SGC-AGD-"));
        assertEquals(EstadoAgendamento.PENDENTE, response.estado());
        assertEquals(TipoAgendamento.PASSAPORTE, response.tipo());
        assertEquals(cidadaoId, response.cidadaoId());
        assertEquals(30, response.duracaoMinutos());
    }

    @Test
    void shouldSetCorrectDurationForVisto() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        AgendamentoCreateRequest request = new AgendamentoCreateRequest(
                cidadaoId, TipoAgendamento.VISTO,
                nextMonday.atTime(10, 0), "Entrevista de visto");

        AgendamentoResponse response = agendamentoService.create(request);
        assertEquals(60, response.duracaoMinutos());
    }

    @Test
    void shouldDetectConflict() {
        AgendamentoCreateRequest request = passaporteRequest();
        agendamentoService.create(request);

        assertThrows(ConflictingAppointmentException.class,
                () -> agendamentoService.create(request));
    }

    @Test
    void shouldFollowStateTransitions() {
        AgendamentoResponse agendamento = agendamentoService.create(passaporteRequest());
        assertEquals(EstadoAgendamento.PENDENTE, agendamento.estado());

        agendamento = agendamentoService.updateEstado(agendamento.id(), EstadoAgendamento.CONFIRMADO, "Confirmado");
        assertEquals(EstadoAgendamento.CONFIRMADO, agendamento.estado());

        agendamento = agendamentoService.updateEstado(agendamento.id(), EstadoAgendamento.COMPLETADO, "Atendido");
        assertEquals(EstadoAgendamento.COMPLETADO, agendamento.estado());
    }

    @Test
    void shouldRejectInvalidTransition() {
        AgendamentoResponse agendamento = agendamentoService.create(passaporteRequest());

        assertThrows(InvalidStateTransitionException.class, () ->
                agendamentoService.updateEstado(agendamento.id(), EstadoAgendamento.COMPLETADO, "Invalid"));
    }

    @Test
    void shouldReschedule() {
        AgendamentoResponse agendamento = agendamentoService.create(passaporteRequest());
        agendamentoService.updateEstado(agendamento.id(), EstadoAgendamento.CONFIRMADO, "Confirmado");

        LocalDate nextTuesday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        AgendamentoUpdateRequest updateRequest = new AgendamentoUpdateRequest(
                nextTuesday.atTime(10, 0), "Nova data");

        agendamento = agendamentoService.reschedule(agendamento.id(), updateRequest);
        assertEquals(EstadoAgendamento.REAGENDADO, agendamento.estado());
        assertEquals(nextTuesday.atTime(10, 0), agendamento.dataHora());
    }

    @Test
    void shouldTrackHistory() {
        AgendamentoResponse agendamento = agendamentoService.create(passaporteRequest());
        agendamentoService.updateEstado(agendamento.id(), EstadoAgendamento.CONFIRMADO, "Confirmado");
        agendamentoService.updateEstado(agendamento.id(), EstadoAgendamento.COMPLETADO, "Completado");

        var historico = agendamentoService.findHistorico(agendamento.id(), Pageable.unpaged());
        assertEquals(3, historico.getContent().size());
    }

    @Test
    void shouldThrowNotFoundForInvalidCidadao() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        AgendamentoCreateRequest request = new AgendamentoCreateRequest(
                UUID.randomUUID(), TipoAgendamento.PASSAPORTE,
                nextMonday.atTime(9, 0), "Test");

        assertThrows(ResourceNotFoundException.class, () -> agendamentoService.create(request));
    }

    @Test
    void shouldDeleteAgendamento() {
        AgendamentoResponse agendamento = agendamentoService.create(passaporteRequest());
        assertDoesNotThrow(() -> agendamentoService.delete(agendamento.id()));
        assertThrows(ResourceNotFoundException.class, () -> agendamentoService.findById(agendamento.id()));
    }

    @Test
    void shouldSetMotivoCancelamentoOnCancel() {
        AgendamentoResponse agendamento = agendamentoService.create(passaporteRequest());
        agendamentoService.updateEstado(agendamento.id(), EstadoAgendamento.CONFIRMADO, "Confirmado");

        agendamento = agendamentoService.updateEstado(agendamento.id(), EstadoAgendamento.CANCELADO, "Motivo teste");
        assertEquals(EstadoAgendamento.CANCELADO, agendamento.estado());
        assertEquals("Motivo teste", agendamento.motivoCancelamento());
    }
}
