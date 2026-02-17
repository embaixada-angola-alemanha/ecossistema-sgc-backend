package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.AuditEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {

    Page<AuditEventEntity> findByEntityType(String entityType, Pageable pageable);

    Page<AuditEventEntity> findByUserId(String userId, Pageable pageable);

    Page<AuditEventEntity> findByTimestampBetween(Instant start, Instant end, Pageable pageable);

    Page<AuditEventEntity> findByEntityTypeAndTimestampBetween(String entityType, Instant start, Instant end, Pageable pageable);

    Page<AuditEventEntity> findAllByOrderByTimestampDesc(Pageable pageable);

    long countByAction(String action);

    long countByEntityType(String entityType);
}
