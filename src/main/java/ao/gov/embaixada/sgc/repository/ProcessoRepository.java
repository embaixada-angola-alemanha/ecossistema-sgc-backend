package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.Processo;
import ao.gov.embaixada.sgc.enums.EstadoProcesso;
import ao.gov.embaixada.sgc.enums.TipoProcesso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProcessoRepository extends JpaRepository<Processo, UUID> {

    Optional<Processo> findByNumeroProcesso(String numeroProcesso);

    Page<Processo> findByCidadaoId(UUID cidadaoId, Pageable pageable);

    Page<Processo> findByTipo(TipoProcesso tipo, Pageable pageable);

    Page<Processo> findByEstado(EstadoProcesso estado, Pageable pageable);

    Page<Processo> findByResponsavel(String responsavel, Pageable pageable);

    boolean existsByNumeroProcesso(String numeroProcesso);

    long countByEstado(EstadoProcesso estado);

    long countByCidadaoId(UUID cidadaoId);

    @Query("SELECT COUNT(p) FROM Processo p WHERE p.cidadao.id = :cidadaoId AND p.estado = :estado")
    long countByCidadaoIdAndEstado(@Param("cidadaoId") UUID cidadaoId, @Param("estado") EstadoProcesso estado);
}
