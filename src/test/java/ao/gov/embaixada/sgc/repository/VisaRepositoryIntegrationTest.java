package ao.gov.embaixada.sgc.repository;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.entity.VisaApplication;
import ao.gov.embaixada.sgc.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class VisaRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private VisaRepository visaRepository;

    @Autowired
    private CidadaoRepository cidadaoRepository;

    private Cidadao cidadao;

    @BeforeEach
    void setUp() {
        cidadao = new Cidadao();
        cidadao.setNumeroPassaporte("VIS-REPO-" + System.nanoTime());
        cidadao.setNomeCompleto("Visa Repo Test");
        cidadao.setNacionalidade("Angolana");
        cidadao.setEstado(EstadoCidadao.ACTIVO);
        cidadao = cidadaoRepository.save(cidadao);
    }

    private VisaApplication buildVisa(String numero, TipoVisto tipo, EstadoVisto estado) {
        VisaApplication visa = new VisaApplication();
        visa.setCidadao(cidadao);
        visa.setNumeroVisto(numero);
        visa.setTipo(tipo);
        visa.setEstado(estado);
        visa.setNacionalidade("Alemã");
        visa.setMotivo("Teste");
        visa.setValorTaxa(new BigDecimal("60.00"));
        visa.setIsento(false);
        return visa;
    }

    @Test
    void shouldSaveAndFindVisa() {
        VisaApplication saved = visaRepository.save(buildVisa(
                "SGC-VIS-REPO-001", TipoVisto.TURISTA, EstadoVisto.RASCUNHO));
        assertNotNull(saved.getId());

        Optional<VisaApplication> found = visaRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("SGC-VIS-REPO-001", found.get().getNumeroVisto());
    }

    @Test
    void shouldFindByNumeroVisto() {
        visaRepository.save(buildVisa("SGC-VIS-FIND-001", TipoVisto.TURISTA, EstadoVisto.RASCUNHO));

        Optional<VisaApplication> found = visaRepository.findByNumeroVisto("SGC-VIS-FIND-001");
        assertTrue(found.isPresent());
        assertEquals(TipoVisto.TURISTA, found.get().getTipo());
    }

    @Test
    void shouldFindByCidadaoId() {
        visaRepository.save(buildVisa("SGC-VIS-CID-001", TipoVisto.TURISTA, EstadoVisto.RASCUNHO));
        visaRepository.save(buildVisa("SGC-VIS-CID-002", TipoVisto.TRABALHO, EstadoVisto.SUBMETIDO));

        List<VisaApplication> found = visaRepository.findByCidadaoId(cidadao.getId());
        assertEquals(2, found.size());
    }

    @Test
    void shouldFindByEstado() {
        visaRepository.save(buildVisa("SGC-VIS-EST-001", TipoVisto.TURISTA, EstadoVisto.SUBMETIDO));

        List<VisaApplication> found = visaRepository.findByEstado(EstadoVisto.SUBMETIDO);
        assertTrue(found.stream().anyMatch(v -> v.getNumeroVisto().equals("SGC-VIS-EST-001")));
    }

    @Test
    void shouldFindByTipo() {
        visaRepository.save(buildVisa("SGC-VIS-TIPO-001", TipoVisto.DIPLOMATICO, EstadoVisto.RASCUNHO));

        List<VisaApplication> found = visaRepository.findByTipo(TipoVisto.DIPLOMATICO);
        assertTrue(found.stream().anyMatch(v -> v.getNumeroVisto().equals("SGC-VIS-TIPO-001")));
    }

    @Test
    void shouldCountByEstado() {
        visaRepository.save(buildVisa("SGC-VIS-CNT-" + System.nanoTime(), TipoVisto.TURISTA, EstadoVisto.RASCUNHO));

        long count = visaRepository.countByEstado(EstadoVisto.RASCUNHO);
        assertTrue(count >= 1);
    }

    @Test
    void shouldDeleteVisa() {
        VisaApplication saved = visaRepository.save(buildVisa(
                "SGC-VIS-DEL-001", TipoVisto.TURISTA, EstadoVisto.RASCUNHO));
        visaRepository.deleteById(saved.getId());
        assertFalse(visaRepository.findById(saved.getId()).isPresent());
    }
}
