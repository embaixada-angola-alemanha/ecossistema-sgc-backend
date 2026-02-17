package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.service.NotificationTemplateResolver.TemplateInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTemplateResolverTest {

    private final NotificationTemplateResolver resolver = new NotificationTemplateResolver();

    @ParameterizedTest
    @CsvSource({
            "Agendamento,PENDENTE,email/agendamento-criado",
            "Agendamento,CONFIRMADO,email/agendamento-confirmado",
            "Agendamento,REAGENDADO,email/agendamento-reagendado",
            "Agendamento,CANCELADO,email/agendamento-cancelado",
            "Visa,SUBMETIDO,email/visa-submetido",
            "Visa,APROVADO,email/visa-aprovado",
            "Visa,REJEITADO,email/visa-rejeitado",
            "Visa,EMITIDO,email/visa-emitido",
            "RegistoCivil,SUBMETIDO,email/registo-civil-submetido",
            "RegistoCivil,VERIFICADO,email/registo-civil-verificado",
            "RegistoCivil,CERTIFICADO_EMITIDO,email/registo-civil-certificado-emitido",
            "RegistoCivil,REJEITADO,email/registo-civil-rejeitado",
            "ServicoNotarial,SUBMETIDO,email/servico-notarial-submetido",
            "ServicoNotarial,CONCLUIDO,email/servico-notarial-concluido",
            "ServicoNotarial,REJEITADO,email/servico-notarial-rejeitado",
            "Processo,SUBMETIDO,email/processo-submetido",
            "Processo,APROVADO,email/processo-aprovado",
            "Processo,REJEITADO,email/processo-rejeitado",
            "Processo,CONCLUIDO,email/processo-concluido"
    })
    void shouldResolveKnownTemplates(String workflow, String state, String expectedTemplate) {
        Optional<TemplateInfo> result = resolver.resolve(workflow, state);
        assertTrue(result.isPresent());
        assertEquals(expectedTemplate, result.get().templateName());
        assertNotNull(result.get().subject());
        assertFalse(result.get().subject().isBlank());
    }

    @Test
    void shouldReturnEmptyForNonNotificationState() {
        assertTrue(resolver.resolve("Visa", "RASCUNHO").isEmpty());
        assertTrue(resolver.resolve("Visa", "EM_ANALISE").isEmpty());
        assertTrue(resolver.resolve("Visa", "DOCUMENTOS_PENDENTES").isEmpty());
        assertTrue(resolver.resolve("Processo", "RASCUNHO").isEmpty());
        assertTrue(resolver.resolve("Processo", "EM_ANALISE").isEmpty());
    }

    @Test
    void shouldReturnEmptyForUnknownWorkflow() {
        assertTrue(resolver.resolve("Unknown", "SUBMETIDO").isEmpty());
    }

    @Test
    void shouldReturnEmptyForUnknownState() {
        assertTrue(resolver.resolve("Visa", "NONEXISTENT").isEmpty());
    }
}
