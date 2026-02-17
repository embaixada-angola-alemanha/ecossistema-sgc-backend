package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.ServicoNotarial;
import ao.gov.embaixada.sgc.enums.EstadoServicoNotarial;
import ao.gov.embaixada.sgc.enums.TipoServicoNotarial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServicoNotarialRepository extends JpaRepository<ServicoNotarial, UUID> {

    Optional<ServicoNotarial> findByNumeroServico(String numeroServico);

    Page<ServicoNotarial> findByCidadaoId(UUID cidadaoId, Pageable pageable);

    Page<ServicoNotarial> findByTipo(TipoServicoNotarial tipo, Pageable pageable);

    Page<ServicoNotarial> findByEstado(EstadoServicoNotarial estado, Pageable pageable);

    boolean existsByNumeroServico(String numeroServico);

    long countByEstado(EstadoServicoNotarial estado);

    long countByCidadaoId(UUID cidadaoId);
}
