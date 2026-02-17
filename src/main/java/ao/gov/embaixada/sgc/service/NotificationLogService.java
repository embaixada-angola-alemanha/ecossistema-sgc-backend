package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.NotificationLogResponse;
import ao.gov.embaixada.sgc.dto.NotificationMessage;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.entity.NotificationLog;
import ao.gov.embaixada.sgc.enums.EstadoNotificacao;
import ao.gov.embaixada.sgc.repository.CidadaoRepository;
import ao.gov.embaixada.sgc.repository.NotificationLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class NotificationLogService {

    private final NotificationLogRepository logRepository;
    private final CidadaoRepository cidadaoRepository;

    public NotificationLogService(NotificationLogRepository logRepository,
                                   CidadaoRepository cidadaoRepository) {
        this.logRepository = logRepository;
        this.cidadaoRepository = cidadaoRepository;
    }

    public NotificationLog createPendingLog(NotificationMessage message) {
        NotificationLog log = new NotificationLog();
        if (message.cidadaoId() != null) {
            cidadaoRepository.findById(message.cidadaoId()).ifPresent(log::setCidadao);
        }
        log.setToAddress(message.toAddress());
        log.setSubject(message.subject());
        log.setTemplate(message.template());
        log.setWorkflowName(message.workflowName());
        log.setEntityId(message.entityId());
        log.setEstado(EstadoNotificacao.PENDENTE);
        return logRepository.save(log);
    }

    public void markSent(UUID logId) {
        logRepository.findById(logId).ifPresent(log -> {
            log.setEstado(EstadoNotificacao.ENVIADO);
            log.setSentAt(Instant.now());
            logRepository.save(log);
        });
    }

    public void markFailed(UUID logId, String errorMessage) {
        logRepository.findById(logId).ifPresent(log -> {
            log.setEstado(EstadoNotificacao.FALHOU);
            log.setErrorMessage(errorMessage);
            logRepository.save(log);
        });
    }

    @Transactional(readOnly = true)
    public Page<NotificationLogResponse> findByCidadaoId(UUID cidadaoId, Pageable pageable) {
        return logRepository.findByCidadaoIdOrderByCreatedAtDesc(cidadaoId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotificationLogResponse> findAll(Pageable pageable) {
        return logRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    private NotificationLogResponse toResponse(NotificationLog log) {
        return new NotificationLogResponse(
                log.getId(),
                log.getCidadao() != null ? log.getCidadao().getId() : null,
                log.getToAddress(),
                log.getSubject(),
                log.getTemplate(),
                log.getWorkflowName(),
                log.getEntityId(),
                log.getEstado(),
                log.getErrorMessage(),
                log.getSentAt(),
                log.getCreatedAt());
    }
}
