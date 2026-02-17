package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CidadaoLookupService {

    private static final Logger log = LoggerFactory.getLogger(CidadaoLookupService.class);

    private final VisaRepository visaRepository;
    private final RegistoCivilRepository registoCivilRepository;
    private final ServicoNotarialRepository servicoNotarialRepository;
    private final ProcessoRepository processoRepository;
    private final AgendamentoRepository agendamentoRepository;

    public CidadaoLookupService(VisaRepository visaRepository,
                                 RegistoCivilRepository registoCivilRepository,
                                 ServicoNotarialRepository servicoNotarialRepository,
                                 ProcessoRepository processoRepository,
                                 AgendamentoRepository agendamentoRepository) {
        this.visaRepository = visaRepository;
        this.registoCivilRepository = registoCivilRepository;
        this.servicoNotarialRepository = servicoNotarialRepository;
        this.processoRepository = processoRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    public record LookupResult(Cidadao cidadao, Map<String, Object> variables) {}

    public Optional<LookupResult> lookup(UUID entityId, String workflowName) {
        return switch (workflowName) {
            case "Visa" -> visaRepository.findById(entityId)
                    .map(v -> new LookupResult(v.getCidadao(), Map.of(
                            "numero", v.getNumeroVisto(),
                            "tipo", v.getTipo().name())));
            case "RegistoCivil" -> registoCivilRepository.findById(entityId)
                    .map(r -> new LookupResult(r.getCidadao(), Map.of(
                            "numero", r.getNumeroRegisto(),
                            "tipo", r.getTipo().name())));
            case "ServicoNotarial" -> servicoNotarialRepository.findById(entityId)
                    .map(s -> new LookupResult(s.getCidadao(), Map.of(
                            "numero", s.getNumeroServico(),
                            "tipo", s.getTipo().name())));
            case "Processo" -> processoRepository.findById(entityId)
                    .map(p -> new LookupResult(p.getCidadao(), Map.of(
                            "numero", p.getNumeroProcesso(),
                            "tipo", p.getTipo().name())));
            case "Agendamento" -> agendamentoRepository.findById(entityId)
                    .map(a -> new LookupResult(a.getCidadao(), Map.of(
                            "numero", a.getNumeroAgendamento(),
                            "tipo", a.getTipo().name(),
                            "dataHora", a.getDataHora().toString(),
                            "local", a.getLocal() != null ? a.getLocal() : "")));
            default -> {
                log.warn("Unknown workflow name: {}", workflowName);
                yield Optional.empty();
            }
        };
    }
}
