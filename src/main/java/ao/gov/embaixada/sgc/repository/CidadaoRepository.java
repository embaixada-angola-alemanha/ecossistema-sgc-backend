package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.enums.EstadoCidadao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CidadaoRepository extends JpaRepository<Cidadao, UUID> {

    Optional<Cidadao> findByNumeroPassaporte(String numeroPassaporte);

    Page<Cidadao> findByEstado(EstadoCidadao estado, Pageable pageable);

    @Query("SELECT c FROM Cidadao c WHERE LOWER(c.nomeCompleto) LIKE LOWER(CONCAT('%', :nome, '%'))")
    Page<Cidadao> searchByNome(@Param("nome") String nome, Pageable pageable);

    boolean existsByNumeroPassaporte(String numeroPassaporte);

    long countByEstado(EstadoCidadao estado);
}
