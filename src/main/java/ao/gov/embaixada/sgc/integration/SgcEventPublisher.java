package ao.gov.embaixada.sgc.integration;

import ao.gov.embaixada.commons.integration.IntegrationEventPublisher;
import ao.gov.embaixada.commons.integration.event.EventTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Publishes SGC events to the cross-system integration exchange.
 * Consumed by SI (consular data feed), WN (news from activities), and GPJ (monitoring).
 */
@Service
public class SgcEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SgcEventPublisher.class);

    private final IntegrationEventPublisher publisher;

    public SgcEventPublisher(@Nullable IntegrationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void cidadaoCreated(String cidadaoId, String nome, String email) {
        if (publisher == null) return;
        publisher.publish(EventTypes.SOURCE_SGC, EventTypes.SGC_CIDADAO_CREATED, cidadaoId, "Cidadao",
            Map.of("nome", nome, "email", email));
    }

    public void cidadaoUpdated(String cidadaoId, String nome) {
        if (publisher == null) return;
        publisher.publish(EventTypes.SOURCE_SGC, EventTypes.SGC_CIDADAO_UPDATED, cidadaoId, "Cidadao",
            Map.of("nome", nome));
    }

    public void processoStateChanged(String processoId, String tipo, String previousState, String newState) {
        if (publisher == null) return;
        publisher.publish(EventTypes.SOURCE_SGC, EventTypes.SGC_PROCESSO_STATE_CHANGED, processoId, "Processo",
            Map.of("tipo", tipo, "previousState", previousState, "newState", newState));
    }

    public void vistoStateChanged(String vistoId, String tipo, String previousState, String newState) {
        if (publisher == null) return;
        publisher.publish(EventTypes.SOURCE_SGC, EventTypes.SGC_VISTO_STATE_CHANGED, vistoId, "Visto",
            Map.of("tipo", tipo, "previousState", previousState, "newState", newState));
    }

    public void agendamentoCreated(String agendamentoId, String tipo, String data) {
        if (publisher == null) return;
        publisher.publish(EventTypes.SOURCE_SGC, EventTypes.SGC_AGENDAMENTO_CREATED, agendamentoId, "Agendamento",
            Map.of("tipo", tipo, "data", data));
    }

    /**
     * Generic activity event — SI and WN subscribe to these for content creation.
     */
    public void activityCompleted(String entityId, String entityType, String description) {
        if (publisher == null) return;
        publisher.publish(EventTypes.SOURCE_SGC, EventTypes.SGC_ACTIVITY_COMPLETED, entityId, entityType,
            Map.of("description", description));
        log.info("Published SGC activity: entityType={}, entityId={}", entityType, entityId);
    }
}
