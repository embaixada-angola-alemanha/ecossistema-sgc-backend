package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.dto.ServicoNotarialCreateRequest;
import ao.gov.embaixada.sgc.dto.ServicoNotarialResponse;
import ao.gov.embaixada.sgc.dto.ServicoNotarialUpdateRequest;
import ao.gov.embaixada.sgc.enums.EstadoServicoNotarial;
import ao.gov.embaixada.sgc.enums.TipoServicoNotarial;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ServicoNotarialServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ServicoNotarialService servicoNotarialService;

    @Autowired
    private CidadaoService cidadaoService;

    private UUID cidadaoId;

    @BeforeEach
    void setUp() {
        CidadaoCreateRequest cidadaoReq = new CidadaoCreateRequest(
                "NOT-TEST-" + System.nanoTime(), "Test Cidadao Notarial", null,
                null, "Angolana", null,
                null, null, null, null);
        CidadaoResponse cidadao = cidadaoService.create(cidadaoReq);
        cidadaoId = cidadao.id();
    }

    private ServicoNotarialCreateRequest procuracaoRequest() {
        return new ServicoNotarialCreateRequest(
                cidadaoId, TipoServicoNotarial.PROCURACAO,
                "Procuracao para venda de imovel", null, null,
                "Joao Silva", "Maria Santos", "Venda de imovel em Luanda",
                null, null, null,
                null, null,
                null, null);
    }

    private ServicoNotarialCreateRequest legalizacaoRequest() {
        return new ServicoNotarialCreateRequest(
                cidadaoId, TipoServicoNotarial.LEGALIZACAO,
                "Legalizacao de diploma", null, null,
                null, null, null,
                "Diploma Universitario", "Angola", "Universidade Agostinho Neto",
                null, null,
                null, null);
    }

    private ServicoNotarialCreateRequest apostilaRequest() {
        return new ServicoNotarialCreateRequest(
                cidadaoId, TipoServicoNotarial.APOSTILA,
                "Apostila de certidao", null, null,
                null, null, null,
                null, null, null,
                "Certidao de nascimento", "Alemanha",
                null, null);
    }

    private ServicoNotarialCreateRequest copiaCertificadaRequest() {
        return new ServicoNotarialCreateRequest(
                cidadaoId, TipoServicoNotarial.COPIA_CERTIFICADA,
                "Copia certificada de passaporte", null, null,
                null, null, null,
                null, null, null,
                null, null,
                "Passaporte N12345", 3);
    }

    @Test
    void shouldCreateProcuracao() {
        ServicoNotarialResponse response = servicoNotarialService.create(procuracaoRequest());

        assertNotNull(response.id());
        assertNotNull(response.numeroServico());
        assertTrue(response.numeroServico().startsWith("SGC-NOT-"));
        assertEquals(EstadoServicoNotarial.RASCUNHO, response.estado());
        assertEquals(TipoServicoNotarial.PROCURACAO, response.tipo());
        assertEquals(cidadaoId, response.cidadaoId());
        assertEquals("Joao Silva", response.outorgante());
        assertEquals("Maria Santos", response.outorgado());
        assertEquals(0, new BigDecimal("50.00").compareTo(response.valorTaxa()));
        assertFalse(response.taxaPaga());
    }

    @Test
    void shouldCreateLegalizacao() {
        ServicoNotarialResponse response = servicoNotarialService.create(legalizacaoRequest());

        assertNotNull(response.id());
        assertEquals(TipoServicoNotarial.LEGALIZACAO, response.tipo());
        assertEquals("Diploma Universitario", response.documentoOrigem());
        assertEquals("Angola", response.paisOrigem());
        assertEquals(0, new BigDecimal("30.00").compareTo(response.valorTaxa()));
    }

    @Test
    void shouldCreateApostila() {
        ServicoNotarialResponse response = servicoNotarialService.create(apostilaRequest());

        assertNotNull(response.id());
        assertEquals(TipoServicoNotarial.APOSTILA, response.tipo());
        assertEquals("Certidao de nascimento", response.documentoApostilado());
        assertEquals("Alemanha", response.paisDestino());
        assertEquals(0, new BigDecimal("25.00").compareTo(response.valorTaxa()));
    }

    @Test
    void shouldCreateCopiaCertificada() {
        ServicoNotarialResponse response = servicoNotarialService.create(copiaCertificadaRequest());

        assertNotNull(response.id());
        assertEquals(TipoServicoNotarial.COPIA_CERTIFICADA, response.tipo());
        assertEquals("Passaporte N12345", response.documentoOriginalRef());
        assertEquals(3, response.numeroCopias());
        assertEquals(0, new BigDecimal("10.00").compareTo(response.valorTaxa()));
    }

    @Test
    void shouldFollowStateTransitions() {
        ServicoNotarialResponse servico = servicoNotarialService.create(procuracaoRequest());
        assertEquals(EstadoServicoNotarial.RASCUNHO, servico.estado());

        servico = servicoNotarialService.updateEstado(servico.id(), EstadoServicoNotarial.SUBMETIDO, "Submetido");
        assertEquals(EstadoServicoNotarial.SUBMETIDO, servico.estado());
        assertNotNull(servico.dataSubmissao());

        servico = servicoNotarialService.updateEstado(servico.id(), EstadoServicoNotarial.EM_PROCESSAMENTO, "Em processamento");
        assertEquals(EstadoServicoNotarial.EM_PROCESSAMENTO, servico.estado());
    }

    @Test
    void shouldRejectInvalidTransition() {
        ServicoNotarialResponse servico = servicoNotarialService.create(procuracaoRequest());

        assertThrows(InvalidStateTransitionException.class, () ->
                servicoNotarialService.updateEstado(servico.id(), EstadoServicoNotarial.CONCLUIDO, "Invalid"));
    }

    @Test
    void shouldTrackHistory() {
        ServicoNotarialResponse servico = servicoNotarialService.create(procuracaoRequest());
        servicoNotarialService.updateEstado(servico.id(), EstadoServicoNotarial.SUBMETIDO, "Submissao");
        servicoNotarialService.updateEstado(servico.id(), EstadoServicoNotarial.EM_PROCESSAMENTO, "Processamento");

        var historico = servicoNotarialService.findHistorico(servico.id(), Pageable.unpaged());
        assertEquals(3, historico.getContent().size());
    }

    @Test
    void shouldMarkTaxaPaga() {
        ServicoNotarialResponse servico = servicoNotarialService.create(procuracaoRequest());
        assertFalse(servico.taxaPaga());

        servico = servicoNotarialService.markTaxaPaga(servico.id());
        assertTrue(servico.taxaPaga());
    }

    @Test
    void shouldUpdateServico() {
        ServicoNotarialResponse servico = servicoNotarialService.create(procuracaoRequest());

        ServicoNotarialUpdateRequest updateReq = new ServicoNotarialUpdateRequest(
                "Descricao actualizada", "Observacoes actualizadas", "Consul Santos",
                "Joao Silva Jr", "Maria Santos Jr", "Venda de terreno",
                null, null, null,
                null, null,
                null, null);

        ServicoNotarialResponse updated = servicoNotarialService.update(servico.id(), updateReq);
        assertEquals("Descricao actualizada", updated.descricao());
        assertEquals("Consul Santos", updated.responsavel());
        assertEquals("Joao Silva Jr", updated.outorgante());
    }

    @Test
    void shouldThrowNotFoundForInvalidCidadao() {
        ServicoNotarialCreateRequest request = new ServicoNotarialCreateRequest(
                UUID.randomUUID(), TipoServicoNotarial.PROCURACAO,
                null, null, null,
                null, null, null,
                null, null, null,
                null, null,
                null, null);

        assertThrows(ResourceNotFoundException.class, () -> servicoNotarialService.create(request));
    }

    @Test
    void shouldDeleteServico() {
        ServicoNotarialResponse servico = servicoNotarialService.create(procuracaoRequest());
        assertDoesNotThrow(() -> servicoNotarialService.delete(servico.id()));
        assertThrows(ResourceNotFoundException.class, () -> servicoNotarialService.findById(servico.id()));
    }

    @Test
    void shouldFilterByTipo() {
        servicoNotarialService.create(procuracaoRequest());
        servicoNotarialService.create(legalizacaoRequest());

        var procuracoes = servicoNotarialService.findByTipo(TipoServicoNotarial.PROCURACAO, Pageable.unpaged());
        assertTrue(procuracoes.getContent().stream()
                .allMatch(s -> s.tipo() == TipoServicoNotarial.PROCURACAO));

        var legalizacoes = servicoNotarialService.findByTipo(TipoServicoNotarial.LEGALIZACAO, Pageable.unpaged());
        assertTrue(legalizacoes.getContent().stream()
                .allMatch(s -> s.tipo() == TipoServicoNotarial.LEGALIZACAO));
    }
}
