package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.Agendamento;
import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import ao.gov.embaixada.sgc.enums.TipoAgendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {

    Optional<Agendamento> findByNumeroAgendamento(String numeroAgendamento);

    Page<Agendamento> findByCidadaoId(UUID cidadaoId, Pageable pageable);

    Page<Agendamento> findByEstado(EstadoAgendamento estado, Pageable pageable);

    Page<Agendamento> findByTipo(TipoAgendamento tipo, Pageable pageable);

    Page<Agendamento> findByDataHoraBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    boolean existsByDataHoraAndTipoAndEstadoIn(LocalDateTime dataHora, TipoAgendamento tipo,
                                                List<EstadoAgendamento> estados);

    List<Agendamento> findByDataHoraBetweenAndTipoAndEstadoIn(
            LocalDateTime start, LocalDateTime end,
            TipoAgendamento tipo, List<EstadoAgendamento> estados);

    long countByEstadoAndCreatedAtBetween(EstadoAgendamento estado, Instant start, Instant end);

    long countByTipoAndCreatedAtBetween(TipoAgendamento tipo, Instant start, Instant end);

    long countByCreatedAtBetween(Instant start, Instant end);
}
