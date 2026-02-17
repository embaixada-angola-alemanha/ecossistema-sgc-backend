package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.dto.RegistoCivilCreateRequest;
import ao.gov.embaixada.sgc.dto.RegistoCivilResponse;
import ao.gov.embaixada.sgc.dto.RegistoCivilUpdateRequest;
import ao.gov.embaixada.sgc.enums.EstadoRegistoCivil;
import ao.gov.embaixada.sgc.enums.TipoRegistoCivil;
import ao.gov.embaixada.sgc.exception.InvalidStateTransitionException;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class RegistoCivilServiceTest extends AbstractIntegrationTest {

    @Autowired
    private RegistoCivilService registoCivilService;

    @Autowired
    private CidadaoService cidadaoService;

    private UUID cidadaoId;

    @BeforeEach
    void setUp() {
        CidadaoCreateRequest cidadaoReq = new CidadaoCreateRequest(
                "RC-TEST-" + System.nanoTime(), "Test Cidadao RC", null,
                null, "Angolana", null,
                null, null, null, null);
        CidadaoResponse cidadao = cidadaoService.create(cidadaoReq);
        cidadaoId = cidadao.id();
    }

    private RegistoCivilCreateRequest nascimentoRequest() {
        return new RegistoCivilCreateRequest(
                cidadaoId, TipoRegistoCivil.NASCIMENTO,
                LocalDate.of(1990, 5, 15), "Luanda",
                null,
                "Joao Silva", "Maria Silva", "Luanda",
                null, null, null,
                null, null, null);
    }

    private RegistoCivilCreateRequest casamentoRequest() {
        return new RegistoCivilCreateRequest(
                cidadaoId, TipoRegistoCivil.CASAMENTO,
                LocalDate.of(2020, 8, 20), "Berlin",
                null,
                null, null, null,
                "Joao Silva", "Ana Santos", "Comunhao de adquiridos",
                null, null, null);
    }

    private RegistoCivilCreateRequest obitoRequest() {
        return new RegistoCivilCreateRequest(
                cidadaoId, TipoRegistoCivil.OBITO,
                LocalDate.of(2024, 1, 10), null,
                null,
                null, null, null,
                null, null, null,
                "Causa natural", "Berlin", LocalDate.of(2024, 1, 10));
    }

    @Test
    void shouldCreateBirthRegistration() {
        RegistoCivilResponse response = registoCivilService.create(nascimentoRequest());

        assertNotNull(response.id());
        assertNotNull(response.numeroRegisto());
        assertTrue(response.numeroRegisto().startsWith("SGC-NAS-"));
        assertEquals(EstadoRegistoCivil.RASCUNHO, response.estado());
        assertEquals(TipoRegistoCivil.NASCIMENTO, response.tipo());
        assertEquals(cidadaoId, response.cidadaoId());
        assertEquals("Joao Silva", response.nomePai());
        assertEquals("Maria Silva", response.nomeMae());
        assertEquals("Luanda", response.localNascimento());
    }

    @Test
    void shouldCreateMarriageRegistration() {
        RegistoCivilResponse response = registoCivilService.create(casamentoRequest());

        assertNotNull(response.id());
        assertTrue(response.numeroRegisto().startsWith("SGC-CAS-"));
        assertEquals(TipoRegistoCivil.CASAMENTO, response.tipo());
        assertEquals("Joao Silva", response.nomeConjuge1());
        assertEquals("Ana Santos", response.nomeConjuge2());
        assertEquals("Comunhao de adquiridos", response.regimeCasamento());
    }

    @Test
    void shouldCreateDeathRegistration() {
        RegistoCivilResponse response = registoCivilService.create(obitoRequest());

        assertNotNull(response.id());
        assertTrue(response.numeroRegisto().startsWith("SGC-OBI-"));
        assertEquals(TipoRegistoCivil.OBITO, response.tipo());
        assertEquals("Causa natural", response.causaObito());
        assertEquals("Berlin", response.localObito());
        assertEquals(LocalDate.of(2024, 1, 10), response.dataObito());
    }

    @Test
    void shouldFollowStateTransitions() {
        RegistoCivilResponse registo = registoCivilService.create(nascimentoRequest());
        assertEquals(EstadoRegistoCivil.RASCUNHO, registo.estado());

        registo = registoCivilService.updateEstado(registo.id(), EstadoRegistoCivil.SUBMETIDO, "Submetido");
        assertEquals(EstadoRegistoCivil.SUBMETIDO, registo.estado());
        assertNotNull(registo.dataSubmissao());

        registo = registoCivilService.updateEstado(registo.id(), EstadoRegistoCivil.EM_VERIFICACAO, "Em verificacao");
        assertEquals(EstadoRegistoCivil.EM_VERIFICACAO, registo.estado());

        registo = registoCivilService.updateEstado(registo.id(), EstadoRegistoCivil.VERIFICADO, "Verificado");
        assertEquals(EstadoRegistoCivil.VERIFICADO, registo.estado());
        assertNotNull(registo.dataVerificacao());
    }

    @Test
    void shouldRejectInvalidTransition() {
        RegistoCivilResponse registo = registoCivilService.create(nascimentoRequest());

        assertThrows(InvalidStateTransitionException.class, () ->
                registoCivilService.updateEstado(registo.id(), EstadoRegistoCivil.VERIFICADO, "Invalid"));
    }

    @Test
    void shouldTrackHistory() {
        RegistoCivilResponse registo = registoCivilService.create(nascimentoRequest());
        registoCivilService.updateEstado(registo.id(), EstadoRegistoCivil.SUBMETIDO, "Submissao");
        registoCivilService.updateEstado(registo.id(), EstadoRegistoCivil.EM_VERIFICACAO, "Verificacao");

        var historico = registoCivilService.findHistorico(registo.id(), Pageable.unpaged());
        assertEquals(3, historico.getContent().size());
    }

    @Test
    void shouldUpdateRegistration() {
        RegistoCivilResponse registo = registoCivilService.create(nascimentoRequest());

        RegistoCivilUpdateRequest updateReq = new RegistoCivilUpdateRequest(
                LocalDate.of(1990, 6, 20), "Benguela",
                "Observacoes actualizadas", "Consul Santos",
                "Joao Silva Jr", "Maria Silva Jr", "Benguela",
                null, null, null,
                null, null, null);

        RegistoCivilResponse updated = registoCivilService.update(registo.id(), updateReq);
        assertEquals("Benguela", updated.localEvento());
        assertEquals("Observacoes actualizadas", updated.observacoes());
        assertEquals("Consul Santos", updated.responsavel());
    }

    @Test
    void shouldThrowNotFoundForInvalidCidadao() {
        RegistoCivilCreateRequest request = new RegistoCivilCreateRequest(
                UUID.randomUUID(), TipoRegistoCivil.NASCIMENTO,
                null, null, null,
                null, null, null,
                null, null, null,
                null, null, null);

        assertThrows(ResourceNotFoundException.class, () -> registoCivilService.create(request));
    }

    @Test
    void shouldDeleteRegistration() {
        RegistoCivilResponse registo = registoCivilService.create(nascimentoRequest());
        assertDoesNotThrow(() -> registoCivilService.delete(registo.id()));
        assertThrows(ResourceNotFoundException.class, () -> registoCivilService.findById(registo.id()));
    }

    @Test
    void shouldFilterByTipo() {
        registoCivilService.create(nascimentoRequest());
        registoCivilService.create(casamentoRequest());

        var nascimentos = registoCivilService.findByTipo(TipoRegistoCivil.NASCIMENTO, Pageable.unpaged());
        assertTrue(nascimentos.getContent().stream()
                .allMatch(r -> r.tipo() == TipoRegistoCivil.NASCIMENTO));

        var casamentos = registoCivilService.findByTipo(TipoRegistoCivil.CASAMENTO, Pageable.unpaged());
        assertTrue(casamentos.getContent().stream()
                .allMatch(r -> r.tipo() == TipoRegistoCivil.CASAMENTO));
    }
}
