package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.ProcessoHistorico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessoHistoricoRepository extends JpaRepository<ProcessoHistorico, UUID> {

    Page<ProcessoHistorico> findByProcessoIdOrderByCreatedAtDesc(UUID processoId, Pageable pageable);
}
