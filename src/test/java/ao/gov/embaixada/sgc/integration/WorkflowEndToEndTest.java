package ao.gov.embaixada.sgc.integration;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.dto.*;
import ao.gov.embaixada.sgc.enums.*;
import ao.gov.embaixada.sgc.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end workflow integration tests that exercise the complete lifecycle
 * across multiple services with a real PostgreSQL database via TestContainers.
 */
@Transactional
class WorkflowEndToEndTest extends AbstractIntegrationTest {

    @Autowired
    private CidadaoService cidadaoService;

    @Autowired
    private VisaService visaService;

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private ProcessoService processoService;

    @Autowired
    private RegistoCivilService registoCivilService;

    @Autowired
    private ServicoNotarialService servicoNotarialService;

    @Test
    void shouldCompleteFullVisaWorkflow() {
        // Step 1: Create citizen
        CidadaoCreateRequest cidadaoReq = new CidadaoCreateRequest(
                "E2E-VIS-" + System.nanoTime(), "João da Silva E2E",
                LocalDate.of(1985, 3, 20), Sexo.MASCULINO, "Angolana",
                EstadoCivil.CASADO, "joao.e2e@embaixada.de", "+491234567", "Luanda", "Berlin");
        CidadaoResponse cidadao = cidadaoService.create(cidadaoReq);
        assertNotNull(cidadao.id());
        assertEquals(EstadoCidadao.ACTIVO, cidadao.estado());

        // Step 2: Create visa application
        VisaCreateRequest visaReq = new VisaCreateRequest(
                cidadao.id(), TipoVisto.TRABALHO, "Alemã",
                "Trabalho em empresa alemã",
                LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(7),
                null, "TechCorp GmbH", null);
        VisaResponse visa = visaService.create(visaReq);
        assertNotNull(visa.id());
        assertEquals(EstadoVisto.RASCUNHO, visa.estado());
        assertTrue(visa.numeroVisto().startsWith("SGC-VIS-"));
        assertEquals(0, visa.valorTaxa().compareTo(new java.math.BigDecimal("150.00")));

        // Step 3: Submit visa
        visa = visaService.updateEstado(visa.id(), EstadoVisto.SUBMETIDO, "Documentos entregues");
        assertEquals(EstadoVisto.SUBMETIDO, visa.estado());
        assertNotNull(visa.dataSubmissao());

        // Step 4: Start analysis
        visa = visaService.updateEstado(visa.id(), EstadoVisto.EM_ANALISE, "Consul iniciou análise");
        assertEquals(EstadoVisto.EM_ANALISE, visa.estado());

        // Step 5: Approve
        visa = visaService.updateEstado(visa.id(), EstadoVisto.APROVADO, "Aprovado pelo consul");
        assertEquals(EstadoVisto.APROVADO, visa.estado());
        assertNotNull(visa.dataDecisao());

        // Step 6: Issue visa (terminal state)
        visa = visaService.updateEstado(visa.id(), EstadoVisto.EMITIDO, "Visto emitido e colado no passaporte");
        assertEquals(EstadoVisto.EMITIDO, visa.estado());

        // Verify history has all transitions
        var historico = visaService.findHistorico(visa.id(), Pageable.unpaged());
        assertEquals(5, historico.getContent().size()); // RASCUNHO creation + 4 transitions
    }

    @Test
    void shouldCompleteAppointmentWorkflow() {
        // Step 1: Create citizen
        CidadaoResponse cidadao = cidadaoService.create(new CidadaoCreateRequest(
                "E2E-AGD-" + System.nanoTime(), "Maria Santos E2E", null,
                Sexo.FEMININO, "Angolana", null,
                "maria.e2e@embaixada.de", null, null, null));

        // Step 2: Schedule appointment (use far-future date to avoid conflicts)
        AgendamentoCreateRequest agdReq = new AgendamentoCreateRequest(
                cidadao.id(), TipoAgendamento.PASSAPORTE,
                LocalDate.of(2098, 3, 2).atTime(9, 0), "Renovação de passaporte");
        AgendamentoResponse agd = agendamentoService.create(agdReq);
        assertNotNull(agd.id());
        assertEquals(EstadoAgendamento.PENDENTE, agd.estado());
        assertEquals(30, agd.duracaoMinutos());

        // Step 3: Confirm
        agd = agendamentoService.updateEstado(agd.id(), EstadoAgendamento.CONFIRMADO, "SMS enviado");
        assertEquals(EstadoAgendamento.CONFIRMADO, agd.estado());

        // Step 4: Complete
        agd = agendamentoService.updateEstado(agd.id(), EstadoAgendamento.COMPLETADO, "Cidadão atendido");
        assertEquals(EstadoAgendamento.COMPLETADO, agd.estado());

        // Verify history
        var historico = agendamentoService.findHistorico(agd.id(), Pageable.unpaged());
        assertEquals(3, historico.getContent().size());
    }

    @Test
    void shouldHandleVisaRejectionWorkflow() {
        CidadaoResponse cidadao = cidadaoService.create(new CidadaoCreateRequest(
                "E2E-REJ-" + System.nanoTime(), "Pedro Neto E2E", null,
                Sexo.MASCULINO, "Angolana", null, null, null, null, null));

        VisaCreateRequest visaReq = new VisaCreateRequest(
                cidadao.id(), TipoVisto.TURISTA, "Alemã",
                "Turismo", LocalDate.now().plusMonths(1), null, null, null, null);
        VisaResponse visa = visaService.create(visaReq);

        visa = visaService.updateEstado(visa.id(), EstadoVisto.SUBMETIDO, "Submetido");
        visa = visaService.updateEstado(visa.id(), EstadoVisto.EM_ANALISE, "Análise");
        visa = visaService.updateEstado(visa.id(), EstadoVisto.REJEITADO, "Documentos insuficientes");
        assertEquals(EstadoVisto.REJEITADO, visa.estado());

        var historico = visaService.findHistorico(visa.id(), Pageable.unpaged());
        assertEquals(4, historico.getContent().size());
    }

    @Test
    void shouldHandleAppointmentCancellationAndReschedule() {
        CidadaoResponse cidadao = cidadaoService.create(new CidadaoCreateRequest(
                "E2E-RESCHED-" + System.nanoTime(), "Ana E2E", null,
                null, "Angolana", null, null, null, null, null));

        AgendamentoResponse agd = agendamentoService.create(new AgendamentoCreateRequest(
                cidadao.id(), TipoAgendamento.VISTO,
                LocalDate.of(2098, 4, 1).atTime(10, 0), "Entrevista de visto"));
        assertEquals(60, agd.duracaoMinutos());

        // Confirm
        agd = agendamentoService.updateEstado(agd.id(), EstadoAgendamento.CONFIRMADO, "Confirmado");

        // Reschedule
        agd = agendamentoService.reschedule(agd.id(),
                new AgendamentoUpdateRequest(LocalDate.of(2098, 4, 2).atTime(14, 0), "Cidadão pediu nova data"));
        assertEquals(EstadoAgendamento.REAGENDADO, agd.estado());
        assertEquals(LocalDate.of(2098, 4, 2).atTime(14, 0), agd.dataHora());
    }

    @Test
    void shouldProcessCivilRegistryWorkflow() {
        CidadaoResponse cidadao = cidadaoService.create(new CidadaoCreateRequest(
                "E2E-RC-" + System.nanoTime(), "Registo Civil E2E", null,
                null, "Angolana", null, null, null, null, null));

        RegistoCivilCreateRequest rcReq = new RegistoCivilCreateRequest(
                cidadao.id(), TipoRegistoCivil.NASCIMENTO, LocalDate.of(2000, 1, 15),
                "Luanda", "Registo de nascimento em Luanda",
                "João Pai", "Maria Mãe", "Luanda",
                null, null, null,
                null, null, null);
        var rc = registoCivilService.create(rcReq);
        assertNotNull(rc.id());
        assertEquals(EstadoRegistoCivil.RASCUNHO, rc.estado());

        rc = registoCivilService.updateEstado(rc.id(), EstadoRegistoCivil.SUBMETIDO, "Submetido");
        assertEquals(EstadoRegistoCivil.SUBMETIDO, rc.estado());

        rc = registoCivilService.updateEstado(rc.id(), EstadoRegistoCivil.EM_VERIFICACAO, "Em verificação");
        rc = registoCivilService.updateEstado(rc.id(), EstadoRegistoCivil.VERIFICADO, "Verificado");
        rc = registoCivilService.updateEstado(rc.id(), EstadoRegistoCivil.CERTIFICADO_EMITIDO, "Certificado emitido");
        assertEquals(EstadoRegistoCivil.CERTIFICADO_EMITIDO, rc.estado());
    }

    @Test
    void shouldProcessNotarialServiceWorkflow() {
        CidadaoResponse cidadao = cidadaoService.create(new CidadaoCreateRequest(
                "E2E-SN-" + System.nanoTime(), "Notarial E2E", null,
                null, "Angolana", null, null, null, null, null));

        ServicoNotarialCreateRequest snReq = new ServicoNotarialCreateRequest(
                cidadao.id(), TipoServicoNotarial.PROCURACAO, "Procuração para venda de imóvel",
                null, null,
                "João Outorgante", "Maria Outorgada", "Venda de imóvel em Luanda",
                null, null, null,
                null, null,
                null, null);
        var sn = servicoNotarialService.create(snReq);
        assertNotNull(sn.id());
        assertEquals(EstadoServicoNotarial.RASCUNHO, sn.estado());

        sn = servicoNotarialService.updateEstado(sn.id(), EstadoServicoNotarial.SUBMETIDO, "Submetido");
        sn = servicoNotarialService.updateEstado(sn.id(), EstadoServicoNotarial.EM_PROCESSAMENTO, "Em processamento");
        sn = servicoNotarialService.updateEstado(sn.id(), EstadoServicoNotarial.CONCLUIDO, "Documento emitido");
        assertEquals(EstadoServicoNotarial.CONCLUIDO, sn.estado());
    }

    @Test
    void shouldCrossReferenceEntitiesByCidadao() {
        CidadaoResponse cidadao = cidadaoService.create(new CidadaoCreateRequest(
                "E2E-XREF-" + System.nanoTime(), "Cross Ref E2E", null,
                null, "Angolana", null, null, null, null, null));

        // Create a visa
        visaService.create(new VisaCreateRequest(
                cidadao.id(), TipoVisto.TURISTA, "Alemã",
                "Turismo", LocalDate.now().plusMonths(1), null, null, null, null));

        // Create an appointment (use far-future date to avoid conflicts)
        agendamentoService.create(new AgendamentoCreateRequest(
                cidadao.id(), TipoAgendamento.PASSAPORTE,
                LocalDate.of(2098, 5, 1).atTime(9, 0), "Passaporte"));

        // Verify both linked to same cidadao
        Page<AgendamentoResponse> agds = agendamentoService.findByCidadaoId(cidadao.id(), Pageable.unpaged());
        assertTrue(agds.getTotalElements() >= 1);
        assertEquals(cidadao.id(), agds.getContent().get(0).cidadaoId());
    }
}
