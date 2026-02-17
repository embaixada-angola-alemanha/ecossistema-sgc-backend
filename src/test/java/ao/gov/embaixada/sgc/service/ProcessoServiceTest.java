package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.dto.ProcessoCreateRequest;
import ao.gov.embaixada.sgc.dto.ProcessoResponse;
import ao.gov.embaixada.sgc.enums.*;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ProcessoServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ProcessoService processoService;

    @Autowired
    private CidadaoService cidadaoService;

    private UUID cidadaoId;

    @BeforeEach
    void setUp() {
        CidadaoCreateRequest cidadaoReq = new CidadaoCreateRequest(
                "PROC-TEST-" + System.nanoTime(), "Test Cidadao", null,
                null, "Angolana", null,
                null, null, null, null);
        CidadaoResponse cidadao = cidadaoService.create(cidadaoReq);
        cidadaoId = cidadao.id();
    }

    @Test
    void shouldCreateProcesso() {
        ProcessoCreateRequest request = new ProcessoCreateRequest(
                cidadaoId, TipoProcesso.PASSAPORTE,
                "Renovacao de passaporte", Prioridade.NORMAL,
                "funcionario1", new BigDecimal("50.00"));

        ProcessoResponse response = processoService.create(request);

        assertNotNull(response.id());
        assertNotNull(response.numeroProcesso());
        assertTrue(response.numeroProcesso().startsWith("SGC-"));
        assertEquals(EstadoProcesso.RASCUNHO, response.estado());
        assertEquals(cidadaoId, response.cidadaoId());
    }

    @Test
    void shouldFollowStateTransitions() {
        ProcessoCreateRequest request = new ProcessoCreateRequest(
                cidadaoId, TipoProcesso.VISTO,
                "Pedido de visto", Prioridade.ALTA,
                null, BigDecimal.ZERO);

        ProcessoResponse processo = processoService.create(request);
        assertEquals(EstadoProcesso.RASCUNHO, processo.estado());

        processo = processoService.updateEstado(processo.id(), EstadoProcesso.SUBMETIDO, "Submetido pelo cidadao");
        assertEquals(EstadoProcesso.SUBMETIDO, processo.estado());
        assertNotNull(processo.dataSubmissao());

        processo = processoService.updateEstado(processo.id(), EstadoProcesso.EM_ANALISE, "Em analise");
        assertEquals(EstadoProcesso.EM_ANALISE, processo.estado());

        processo = processoService.updateEstado(processo.id(), EstadoProcesso.APROVADO, "Aprovado");
        assertEquals(EstadoProcesso.APROVADO, processo.estado());

        processo = processoService.updateEstado(processo.id(), EstadoProcesso.CONCLUIDO, "Concluido");
        assertEquals(EstadoProcesso.CONCLUIDO, processo.estado());
        assertNotNull(processo.dataConclusao());
    }

    @Test
    void shouldRejectInvalidTransition() {
        ProcessoCreateRequest request = new ProcessoCreateRequest(
                cidadaoId, TipoProcesso.LEGALIZACAO,
                "Legalizacao", Prioridade.NORMAL,
                null, BigDecimal.ZERO);

        ProcessoResponse processo = processoService.create(request);

        assertThrows(InvalidStateTransitionException.class, () ->
                processoService.updateEstado(processo.id(), EstadoProcesso.APROVADO, "Invalid"));
    }

    @Test
    void shouldTrackHistory() {
        ProcessoCreateRequest request = new ProcessoCreateRequest(
                cidadaoId, TipoProcesso.CERTIDAO,
                "Certidao de nascimento", Prioridade.BAIXA,
                null, BigDecimal.ZERO);

        ProcessoResponse processo = processoService.create(request);
        processoService.updateEstado(processo.id(), EstadoProcesso.SUBMETIDO, "Submissao");
        processoService.updateEstado(processo.id(), EstadoProcesso.EM_ANALISE, "Analise");

        var historico = processoService.findHistorico(processo.id(), Pageable.unpaged());
        assertEquals(3, historico.getContent().size());
    }

    @Test
    void shouldThrowNotFoundForInvalidCidadao() {
        ProcessoCreateRequest request = new ProcessoCreateRequest(
                UUID.randomUUID(), TipoProcesso.PASSAPORTE,
                "Test", null, null, null);

        assertThrows(ResourceNotFoundException.class, () -> processoService.create(request));
    }
}
