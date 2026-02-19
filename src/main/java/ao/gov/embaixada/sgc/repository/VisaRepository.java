package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.VisaApplication;
import ao.gov.embaixada.sgc.enums.EstadoVisto;
import ao.gov.embaixada.sgc.enums.TipoVisto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface VisaRepository extends JpaRepository<VisaApplication, UUID> {

    Optional<VisaApplication> findByNumeroVisto(String numeroVisto);

    Page<VisaApplication> findByCidadaoId(UUID cidadaoId, Pageable pageable);

    Page<VisaApplication> findByTipo(TipoVisto tipo, Pageable pageable);

    Page<VisaApplication> findByEstado(EstadoVisto estado, Pageable pageable);

    boolean existsByNumeroVisto(String numeroVisto);

    long countByEstado(EstadoVisto estado);

    long countByCidadaoId(UUID cidadaoId);

    long countByCidadaoIdAndEstado(UUID cidadaoId, EstadoVisto estado);

    long countByEstadoAndCreatedAtBetween(EstadoVisto estado, Instant start, Instant end);

    long countByTipoAndCreatedAtBetween(TipoVisto tipo, Instant start, Instant end);

    long countByCreatedAtBetween(Instant start, Instant end);
}
