package ao.gov.embaixada.sgc.statemachine.event;

import ao.gov.embaixada.sgc.config.RabbitMQConfig;
import ao.gov.embaixada.sgc.dto.NotificationMessage;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.service.CidadaoLookupService;
import ao.gov.embaixada.sgc.service.CidadaoLookupService.LookupResult;
import ao.gov.embaixada.sgc.service.NotificationPreferenceService;
import ao.gov.embaixada.sgc.service.NotificationTemplateResolver;
import ao.gov.embaixada.sgc.service.NotificationTemplateResolver.TemplateInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowNotificationListenerTest {

    @Mock
    private NotificationTemplateResolver templateResolver;

    @Mock
    private CidadaoLookupService cidadaoLookupService;

    @Mock
    private NotificationPreferenceService preferenceService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private WorkflowNotificationListener listener;

    @Test
    void shouldQueueNotificationForMatchingTransition() {
        UUID entityId = UUID.randomUUID();
        UUID cidadaoId = UUID.randomUUID();
        WorkflowTransitionEvent event = new WorkflowTransitionEvent(
                this, entityId, "Visa", "RASCUNHO", "SUBMETIDO", "Submissao");

        Cidadao cidadao = new Cidadao();
        cidadao.setId(cidadaoId);
        cidadao.setNomeCompleto("Test User");
        cidadao.setEmail("test@example.com");

        when(templateResolver.resolve("Visa", "SUBMETIDO"))
                .thenReturn(Optional.of(new TemplateInfo("email/visa-submetido", "Pedido de Visto Submetido")));
        when(cidadaoLookupService.lookup(entityId, "Visa"))
                .thenReturn(Optional.of(new LookupResult(cidadao, Map.of("numero", "V-001", "tipo", "TRABALHO"))));
        when(preferenceService.isNotificationEnabled(cidadaoId, "Visa")).thenReturn(true);

        listener.onWorkflowTransition(event);

        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.NOTIFICATION_ROUTING_KEY),
                captor.capture());

        NotificationMessage msg = captor.getValue();
        assertEquals("test@example.com", msg.toAddress());
        assertEquals("Pedido de Visto Submetido", msg.subject());
        assertEquals("email/visa-submetido", msg.template());
        assertEquals("Test User", msg.variables().get("nome"));
        assertEquals("V-001", msg.variables().get("numero"));
    }

    @Test
    void shouldSkipWhenNoTemplateMatches() {
        WorkflowTransitionEvent event = new WorkflowTransitionEvent(
                this, UUID.randomUUID(), "Visa", null, "RASCUNHO", null);

        when(templateResolver.resolve("Visa", "RASCUNHO")).thenReturn(Optional.empty());

        listener.onWorkflowTransition(event);

        verifyNoInteractions(cidadaoLookupService, rabbitTemplate);
    }

    @Test
    void shouldSkipWhenCidadaoHasNoEmail() {
        UUID entityId = UUID.randomUUID();
        WorkflowTransitionEvent event = new WorkflowTransitionEvent(
                this, entityId, "Visa", "RASCUNHO", "SUBMETIDO", null);

        Cidadao cidadao = new Cidadao();
        cidadao.setId(UUID.randomUUID());
        cidadao.setNomeCompleto("No Email User");

        when(templateResolver.resolve("Visa", "SUBMETIDO"))
                .thenReturn(Optional.of(new TemplateInfo("email/visa-submetido", "Subject")));
        when(cidadaoLookupService.lookup(entityId, "Visa"))
                .thenReturn(Optional.of(new LookupResult(cidadao, Map.of("numero", "V-002", "tipo", "TURISTA"))));

        listener.onWorkflowTransition(event);

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void shouldSkipWhenNotificationDisabledByPreference() {
        UUID entityId = UUID.randomUUID();
        UUID cidadaoId = UUID.randomUUID();
        WorkflowTransitionEvent event = new WorkflowTransitionEvent(
                this, entityId, "Visa", "RASCUNHO", "SUBMETIDO", null);

        Cidadao cidadao = new Cidadao();
        cidadao.setId(cidadaoId);
        cidadao.setNomeCompleto("Disabled User");
        cidadao.setEmail("disabled@example.com");

        when(templateResolver.resolve("Visa", "SUBMETIDO"))
                .thenReturn(Optional.of(new TemplateInfo("email/visa-submetido", "Subject")));
        when(cidadaoLookupService.lookup(entityId, "Visa"))
                .thenReturn(Optional.of(new LookupResult(cidadao, Map.of("numero", "V-003", "tipo", "TURISTA"))));
        when(preferenceService.isNotificationEnabled(cidadaoId, "Visa")).thenReturn(false);

        listener.onWorkflowTransition(event);

        verifyNoInteractions(rabbitTemplate);
    }
}
