package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.commons.notification.NotificationService;
import ao.gov.embaixada.sgc.config.RabbitMQConfig;
import ao.gov.embaixada.sgc.dto.NotificationMessage;
import ao.gov.embaixada.sgc.entity.NotificationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationService notificationService;
    private final NotificationLogService logService;

    public NotificationConsumer(NotificationService notificationService,
                                 NotificationLogService logService) {
        this.notificationService = notificationService;
        this.logService = logService;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consume(NotificationMessage message) {
        NotificationLog notifLog = logService.createPendingLog(message);
        try {
            notificationService.sendEmail(
                    message.toAddress(),
                    message.subject(),
                    message.template(),
                    message.variables());
            logService.markSent(notifLog.getId());
            log.info("Notification sent to {} template={}", message.toAddress(), message.template());
        } catch (Exception e) {
            logService.markFailed(notifLog.getId(), e.getMessage());
            log.warn("Failed to send notification to {}: {}", message.toAddress(), e.getMessage());
        }
    }
}
