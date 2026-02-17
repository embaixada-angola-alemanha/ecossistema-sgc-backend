package ao.gov.embaixada.sgc.statemachine.event;

import ao.gov.embaixada.sgc.config.RabbitMQConfig;
import ao.gov.embaixada.sgc.dto.NotificationMessage;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.service.CidadaoLookupService;
import ao.gov.embaixada.sgc.service.NotificationPreferenceService;
import ao.gov.embaixada.sgc.service.NotificationTemplateResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

@Component
public class WorkflowNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(WorkflowNotificationListener.class);

    private final NotificationTemplateResolver templateResolver;
    private final CidadaoLookupService cidadaoLookupService;
    private final NotificationPreferenceService preferenceService;
    private final RabbitTemplate rabbitTemplate;

    public WorkflowNotificationListener(NotificationTemplateResolver templateResolver,
                                         CidadaoLookupService cidadaoLookupService,
                                         NotificationPreferenceService preferenceService,
                                         @Nullable RabbitTemplate rabbitTemplate) {
        this.templateResolver = templateResolver;
        this.cidadaoLookupService = cidadaoLookupService;
        this.preferenceService = preferenceService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowTransition(WorkflowTransitionEvent event) {
        templateResolver.resolve(event.getWorkflowName(), event.getNewState())
                .ifPresent(templateInfo -> processNotification(event, templateInfo));
    }

    private void processNotification(WorkflowTransitionEvent event,
                                      NotificationTemplateResolver.TemplateInfo templateInfo) {
        cidadaoLookupService.lookup(event.getEntityId(), event.getWorkflowName())
                .ifPresentOrElse(
                        result -> sendNotification(event, templateInfo, result),
                        () -> log.warn("Could not find entity {} for workflow {}",
                                event.getEntityId(), event.getWorkflowName()));
    }

    private void sendNotification(WorkflowTransitionEvent event,
                                   NotificationTemplateResolver.TemplateInfo templateInfo,
                                   CidadaoLookupService.LookupResult lookupResult) {
        Cidadao cidadao = lookupResult.cidadao();
        String email = cidadao.getEmail();

        if (email == null || email.isBlank()) {
            log.debug("Cidadao {} has no email, skipping notification", cidadao.getId());
            return;
        }

        if (!preferenceService.isNotificationEnabled(cidadao.getId(), event.getWorkflowName())) {
            log.debug("Notifications disabled for cidadao {} workflow {}",
                    cidadao.getId(), event.getWorkflowName());
            return;
        }

        Map<String, Object> variables = new HashMap<>(lookupResult.variables());
        variables.put("nome", cidadao.getNomeCompleto());
        variables.put("estado", event.getNewState());
        if (event.getComment() != null) {
            variables.put("comentario", event.getComment());
        }

        NotificationMessage message = new NotificationMessage(
                cidadao.getId(),
                email,
                templateInfo.subject(),
                templateInfo.templateName(),
                variables,
                event.getWorkflowName(),
                event.getEntityId());

        if (rabbitTemplate != null) {
            try {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.NOTIFICATION_EXCHANGE,
                        RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                        message);
                log.debug("Notification queued for {} template={}", email, templateInfo.templateName());
            } catch (Exception e) {
                log.warn("Failed to queue notification for {}: {}", email, e.getMessage());
            }
        } else {
            log.debug("RabbitMQ not available, notification skipped for {}", email);
        }
    }
}
