package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.enums.EstadoCidadao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CidadaoRepository extends JpaRepository<Cidadao, UUID>, JpaSpecificationExecutor<Cidadao> {

    Optional<Cidadao> findByNumeroPassaporte(String numeroPassaporte);

    Optional<Cidadao> findByKeycloakId(String keycloakId);

    Page<Cidadao> findByEstado(EstadoCidadao estado, Pageable pageable);

    boolean existsByNumeroPassaporte(String numeroPassaporte);

    long countByEstado(EstadoCidadao estado);
}
