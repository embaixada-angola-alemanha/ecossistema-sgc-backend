package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.AgendamentoHistorico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AgendamentoHistoricoRepository extends JpaRepository<AgendamentoHistorico, UUID> {

    Page<AgendamentoHistorico> findByAgendamentoIdOrderByCreatedAtDesc(UUID agendamentoId, Pageable pageable);
}
