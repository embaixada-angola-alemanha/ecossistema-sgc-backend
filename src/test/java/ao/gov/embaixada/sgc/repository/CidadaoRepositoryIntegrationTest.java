package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.enums.EstadoCidadao;
import ao.gov.embaixada.sgc.enums.Sexo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class CidadaoRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CidadaoRepository cidadaoRepository;

    private Cidadao buildCidadao(String passport) {
        Cidadao c = new Cidadao();
        c.setNumeroPassaporte(passport);
        c.setNomeCompleto("Repo Test " + passport);
        c.setNacionalidade("Angolana");
        c.setEstado(EstadoCidadao.ACTIVO);
        c.setDataNascimento(LocalDate.of(1990, 1, 1));
        c.setSexo(Sexo.MASCULINO);
        return c;
    }

    @Test
    void shouldSaveAndRetrieveCidadao() {
        Cidadao saved = cidadaoRepository.save(buildCidadao("REPO-001"));
        assertNotNull(saved.getId());

        Optional<Cidadao> found = cidadaoRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("REPO-001", found.get().getNumeroPassaporte());
    }

    @Test
    void shouldFindByNumeroPassaporte() {
        cidadaoRepository.save(buildCidadao("REPO-PASS-001"));

        Optional<Cidadao> found = cidadaoRepository.findByNumeroPassaporte("REPO-PASS-001");
        assertTrue(found.isPresent());
        assertEquals("Repo Test REPO-PASS-001", found.get().getNomeCompleto());
    }

    @Test
    void shouldCheckExistsByNumeroPassaporte() {
        cidadaoRepository.save(buildCidadao("REPO-EXISTS-001"));

        assertTrue(cidadaoRepository.existsByNumeroPassaporte("REPO-EXISTS-001"));
        assertFalse(cidadaoRepository.existsByNumeroPassaporte("NONEXISTENT"));
    }

    @Test
    void shouldCountByEstado() {
        cidadaoRepository.save(buildCidadao("REPO-COUNT-" + System.nanoTime()));

        long count = cidadaoRepository.countByEstado(EstadoCidadao.ACTIVO);
        assertTrue(count >= 1);
    }

    @Test
    void shouldFindByEstado() {
        cidadaoRepository.save(buildCidadao("REPO-EST-" + System.nanoTime()));

        var result = cidadaoRepository.findByEstado(EstadoCidadao.ACTIVO);
        assertTrue(result.size() >= 1);
        assertTrue(result.stream().allMatch(c -> c.getEstado() == EstadoCidadao.ACTIVO));
    }

    @Test
    void shouldDeleteCidadao() {
        Cidadao saved = cidadaoRepository.save(buildCidadao("REPO-DEL-" + System.nanoTime()));
        cidadaoRepository.deleteById(saved.getId());

        assertFalse(cidadaoRepository.findById(saved.getId()).isPresent());
    }
}
