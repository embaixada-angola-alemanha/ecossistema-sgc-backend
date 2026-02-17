package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.RegistoCivil;
import ao.gov.embaixada.sgc.enums.EstadoRegistoCivil;
import ao.gov.embaixada.sgc.enums.TipoRegistoCivil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RegistoCivilRepository extends JpaRepository<RegistoCivil, UUID> {

    Optional<RegistoCivil> findByNumeroRegisto(String numeroRegisto);

    Page<RegistoCivil> findByCidadaoId(UUID cidadaoId, Pageable pageable);

    Page<RegistoCivil> findByTipo(TipoRegistoCivil tipo, Pageable pageable);

    Page<RegistoCivil> findByEstado(EstadoRegistoCivil estado, Pageable pageable);

    boolean existsByNumeroRegisto(String numeroRegisto);

    long countByEstado(EstadoRegistoCivil estado);

    long countByCidadaoId(UUID cidadaoId);

    long countByEstadoAndCreatedAtBetween(EstadoRegistoCivil estado, Instant start, Instant end);

    long countByTipoAndCreatedAtBetween(TipoRegistoCivil tipo, Instant start, Instant end);

    long countByCreatedAtBetween(Instant start, Instant end);
}
