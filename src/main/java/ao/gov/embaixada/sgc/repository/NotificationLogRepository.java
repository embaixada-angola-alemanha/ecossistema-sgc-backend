package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    Page<NotificationLog> findByCidadaoIdOrderByCreatedAtDesc(UUID cidadaoId, Pageable pageable);

    Page<NotificationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
