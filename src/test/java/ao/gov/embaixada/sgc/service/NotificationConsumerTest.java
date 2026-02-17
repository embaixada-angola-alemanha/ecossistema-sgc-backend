package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.commons.notification.NotificationService;
import ao.gov.embaixada.sgc.dto.NotificationMessage;
import ao.gov.embaixada.sgc.entity.NotificationLog;
import ao.gov.embaixada.sgc.enums.EstadoNotificacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationLogService logService;

    @InjectMocks
    private NotificationConsumer consumer;

    @Test
    void shouldSendEmailAndMarkSent() {
        UUID entityId = UUID.randomUUID();
        NotificationMessage message = new NotificationMessage(
                UUID.randomUUID(), "test@example.com", "Test Subject",
                "email/visa-submetido", Map.of("nome", "Test"), "Visa", entityId);

        NotificationLog mockLog = new NotificationLog();
        mockLog.setId(UUID.randomUUID());
        mockLog.setEstado(EstadoNotificacao.PENDENTE);
        when(logService.createPendingLog(any())).thenReturn(mockLog);

        consumer.consume(message);

        verify(notificationService).sendEmail(
                eq("test@example.com"),
                eq("Test Subject"),
                eq("email/visa-submetido"),
                eq(Map.of("nome", "Test")));
        verify(logService).markSent(mockLog.getId());
        verify(logService, never()).markFailed(any(), any());
    }

    @Test
    void shouldMarkFailedOnException() {
        NotificationMessage message = new NotificationMessage(
                UUID.randomUUID(), "test@example.com", "Test Subject",
                "email/visa-submetido", Map.of(), "Visa", UUID.randomUUID());

        NotificationLog mockLog = new NotificationLog();
        mockLog.setId(UUID.randomUUID());
        when(logService.createPendingLog(any())).thenReturn(mockLog);

        doThrow(new RuntimeException("SMTP error"))
                .when(notificationService).sendEmail(any(), any(), any(), any());

        consumer.consume(message);

        verify(logService).markFailed(mockLog.getId(), "SMTP error");
        verify(logService, never()).markSent(any());
    }
}
