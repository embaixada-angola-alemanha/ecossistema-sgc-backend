package ao.gov.embaixada.sgc.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class NotificationTemplateResolver {

    public record TemplateInfo(String templateName, String subject) {}

    private static final Map<String, Map<String, TemplateInfo>> TEMPLATE_MAP = Map.ofEntries(
            // Agendamento
            Map.entry("Agendamento", Map.of(
                    "PENDENTE", new TemplateInfo("email/agendamento-criado", "Agendamento Criado"),
                    "CONFIRMADO", new TemplateInfo("email/agendamento-confirmado", "Agendamento Confirmado"),
                    "REAGENDADO", new TemplateInfo("email/agendamento-reagendado", "Agendamento Reagendado"),
                    "CANCELADO", new TemplateInfo("email/agendamento-cancelado", "Agendamento Cancelado")
            )),
            // Visa
            Map.entry("Visa", Map.of(
                    "SUBMETIDO", new TemplateInfo("email/visa-submetido", "Pedido de Visto Submetido"),
                    "APROVADO", new TemplateInfo("email/visa-aprovado", "Pedido de Visto Aprovado"),
                    "REJEITADO", new TemplateInfo("email/visa-rejeitado", "Pedido de Visto Rejeitado"),
                    "EMITIDO", new TemplateInfo("email/visa-emitido", "Visto Emitido")
            )),
            // RegistoCivil
            Map.entry("RegistoCivil", Map.of(
                    "SUBMETIDO", new TemplateInfo("email/registo-civil-submetido", "Pedido de Registo Civil Submetido"),
                    "VERIFICADO", new TemplateInfo("email/registo-civil-verificado", "Registo Civil Verificado"),
                    "CERTIFICADO_EMITIDO", new TemplateInfo("email/registo-civil-certificado-emitido", "Certificado Emitido"),
                    "REJEITADO", new TemplateInfo("email/registo-civil-rejeitado", "Pedido de Registo Civil Rejeitado")
            )),
            // ServicoNotarial
            Map.entry("ServicoNotarial", Map.of(
                    "SUBMETIDO", new TemplateInfo("email/servico-notarial-submetido", "Servico Notarial Submetido"),
                    "CONCLUIDO", new TemplateInfo("email/servico-notarial-concluido", "Servico Notarial Concluido"),
                    "REJEITADO", new TemplateInfo("email/servico-notarial-rejeitado", "Servico Notarial Rejeitado")
            )),
            // Processo
            Map.entry("Processo", Map.of(
                    "SUBMETIDO", new TemplateInfo("email/processo-submetido", "Processo Submetido"),
                    "APROVADO", new TemplateInfo("email/processo-aprovado", "Processo Aprovado"),
                    "REJEITADO", new TemplateInfo("email/processo-rejeitado", "Processo Rejeitado"),
                    "CONCLUIDO", new TemplateInfo("email/processo-concluido", "Processo Concluido")
            ))
    );

    public Optional<TemplateInfo> resolve(String workflowName, String newState) {
        return Optional.ofNullable(TEMPLATE_MAP.get(workflowName))
                .map(states -> states.get(newState));
    }
}
