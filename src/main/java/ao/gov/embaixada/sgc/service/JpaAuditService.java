package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.commons.audit.AuditEvent;
import ao.gov.embaixada.commons.audit.AuditService;
import ao.gov.embaixada.sgc.entity.AuditEventEntity;
import ao.gov.embaixada.sgc.repository.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JpaAuditService implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(JpaAuditService.class);

    private final AuditEventRepository auditEventRepository;

    public JpaAuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditEvent event) {
        try {
            AuditEventEntity entity = new AuditEventEntity();
            entity.setAction(event.getAction() != null ? event.getAction().name() : "UNKNOWN");
            entity.setEntityType(event.getResourceType());
            entity.setEntityId(event.getResourceId());
            entity.setUserId(event.getUserId());
            entity.setUsername(event.getUserName());
            entity.setDetails(event.getDetails());
            entity.setTimestamp(event.getTimestamp());

            auditEventRepository.save(entity);

            log.debug("AUDIT persisted: {} | user={} | resource={}:{}",
                    event.getAction(), event.getUserName(),
                    event.getResourceType(), event.getResourceId());
        } catch (Exception e) {
            log.warn("Failed to persist audit event: {}", e.getMessage());
        }
    }
}
