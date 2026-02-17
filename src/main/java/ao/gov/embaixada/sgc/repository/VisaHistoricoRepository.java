package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.VisaHistorico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VisaHistoricoRepository extends JpaRepository<VisaHistorico, UUID> {

    Page<VisaHistorico> findByVisaApplicationIdOrderByCreatedAtDesc(UUID visaId, Pageable pageable);
}
