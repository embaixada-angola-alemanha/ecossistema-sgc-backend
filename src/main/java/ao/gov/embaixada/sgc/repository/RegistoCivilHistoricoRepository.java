package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.RegistoCivilHistorico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegistoCivilHistoricoRepository extends JpaRepository<RegistoCivilHistorico, UUID> {

    Page<RegistoCivilHistorico> findByRegistoCivilIdOrderByCreatedAtDesc(UUID registoCivilId, Pageable pageable);
}
