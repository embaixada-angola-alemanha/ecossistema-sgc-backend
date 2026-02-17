package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    List<NotificationPreference> findByCidadaoId(UUID cidadaoId);

    Optional<NotificationPreference> findByCidadaoIdAndWorkflowName(UUID cidadaoId, String workflowName);
}
