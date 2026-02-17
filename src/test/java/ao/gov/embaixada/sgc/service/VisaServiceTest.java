package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.dto.VisaCreateRequest;
import ao.gov.embaixada.sgc.dto.VisaResponse;
import ao.gov.embaixada.sgc.enums.EstadoVisto;
import ao.gov.embaixada.sgc.enums.TipoVisto;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VisaServiceTest {

    @Autowired
    private VisaService visaService;

    @Autowired
    private CidadaoService cidadaoService;

    private UUID cidadaoId;

    @BeforeEach
    void setUp() {
        CidadaoCreateRequest cidadaoReq = new CidadaoCreateRequest(
                "VIS-TEST-" + System.nanoTime(), "Test Cidadao Visa", null,
                null, "Alemã", null,
                null, null, null, null);
        CidadaoResponse cidadao = cidadaoService.create(cidadaoReq);
        cidadaoId = cidadao.id();
    }

    private VisaCreateRequest turistaRequest() {
        return new VisaCreateRequest(
                cidadaoId, TipoVisto.TURISTA, "Alemã",
                "Turismo em Luanda",
                LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(2),
                "Hotel Presidente", null, null);
    }

    @Test
    void shouldCreateVisa() {
        VisaResponse response = visaService.create(turistaRequest());

        assertNotNull(response.id());
        assertNotNull(response.numeroVisto());
        assertTrue(response.numeroVisto().startsWith("SGC-VIS-"));
        assertEquals(EstadoVisto.RASCUNHO, response.estado());
        assertEquals(TipoVisto.TURISTA, response.tipo());
        assertEquals(cidadaoId, response.cidadaoId());
        assertEquals(0, new BigDecimal("60.00").compareTo(response.valorTaxa()));
    }

    @Test
    void shouldCalculateFeeOnCreate() {
        VisaCreateRequest request = new VisaCreateRequest(
                cidadaoId, TipoVisto.TRABALHO, "Alemã",
                "Trabalho em Luanda",
                LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(6),
                null, "Empresa XPTO", null);

        VisaResponse response = visaService.create(request);
        assertEquals(0, new BigDecimal("150.00").compareTo(response.valorTaxa()));
    }

    @Test
    void shouldSetZeroFeeForDiplomatico() {
        VisaCreateRequest request = new VisaCreateRequest(
                cidadaoId, TipoVisto.DIPLOMATICO, "Alemã",
                "Missao diplomatica",
                LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(3),
                null, null, null);

        VisaResponse response = visaService.create(request);
        assertEquals(0, BigDecimal.ZERO.compareTo(response.valorTaxa()));
    }

    @Test
    void shouldFollowStateTransitions() {
        VisaResponse visa = visaService.create(turistaRequest());
        assertEquals(EstadoVisto.RASCUNHO, visa.estado());

        visa = visaService.updateEstado(visa.id(), EstadoVisto.SUBMETIDO, "Submetido pelo cidadao");
        assertEquals(EstadoVisto.SUBMETIDO, visa.estado());
        assertNotNull(visa.dataSubmissao());

        visa = visaService.updateEstado(visa.id(), EstadoVisto.EM_ANALISE, "Em analise pelo consul");
        assertEquals(EstadoVisto.EM_ANALISE, visa.estado());

        visa = visaService.updateEstado(visa.id(), EstadoVisto.APROVADO, "Aprovado");
        assertEquals(EstadoVisto.APROVADO, visa.estado());
        assertNotNull(visa.dataDecisao());

        visa = visaService.updateEstado(visa.id(), EstadoVisto.EMITIDO, "Visto emitido");
        assertEquals(EstadoVisto.EMITIDO, visa.estado());
    }

    @Test
    void shouldRejectInvalidTransition() {
        VisaResponse visa = visaService.create(turistaRequest());

        assertThrows(InvalidStateTransitionException.class, () ->
                visaService.updateEstado(visa.id(), EstadoVisto.APROVADO, "Invalid"));
    }

    @Test
    void shouldTrackHistory() {
        VisaResponse visa = visaService.create(turistaRequest());
        visaService.updateEstado(visa.id(), EstadoVisto.SUBMETIDO, "Submissao");
        visaService.updateEstado(visa.id(), EstadoVisto.EM_ANALISE, "Analise");

        var historico = visaService.findHistorico(visa.id(), Pageable.unpaged());
        assertEquals(3, historico.getContent().size());
    }

    @Test
    void shouldThrowNotFoundForInvalidCidadao() {
        VisaCreateRequest request = new VisaCreateRequest(
                UUID.randomUUID(), TipoVisto.TURISTA, "Alemã",
                "Test", null, null, null, null, null);

        assertThrows(ResourceNotFoundException.class, () -> visaService.create(request));
    }

    @Test
    void shouldDeleteVisa() {
        VisaResponse visa = visaService.create(turistaRequest());
        assertDoesNotThrow(() -> visaService.delete(visa.id()));
        assertThrows(ResourceNotFoundException.class, () -> visaService.findById(visa.id()));
    }
}
