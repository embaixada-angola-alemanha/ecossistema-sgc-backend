package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.ServicoNotarialHistorico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServicoNotarialHistoricoRepository extends JpaRepository<ServicoNotarialHistorico, UUID> {

    Page<ServicoNotarialHistorico> findByServicoNotarialIdOrderByCreatedAtDesc(UUID servicoNotarialId, Pageable pageable);
}
