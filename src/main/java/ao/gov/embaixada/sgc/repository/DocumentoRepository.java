package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.Documento;
import ao.gov.embaixada.sgc.enums.EstadoDocumento;
import ao.gov.embaixada.sgc.enums.TipoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentoRepository extends JpaRepository<Documento, UUID> {

    Page<Documento> findByCidadaoId(UUID cidadaoId, Pageable pageable);

    Page<Documento> findByTipo(TipoDocumento tipo, Pageable pageable);

    Page<Documento> findByEstado(EstadoDocumento estado, Pageable pageable);

    Page<Documento> findByCidadaoIdAndTipo(UUID cidadaoId, TipoDocumento tipo, Pageable pageable);

    long countByCidadaoId(UUID cidadaoId);

    List<Documento> findByDocumentoOriginalIdOrderByVersaoDesc(UUID documentoOriginalId);

    @Query("SELECT COALESCE(MAX(d.versao), 0) FROM Documento d WHERE d.documentoOriginal.id = :originalId OR d.id = :originalId")
    int findMaxVersaoByOriginalId(@Param("originalId") UUID originalId);
}
