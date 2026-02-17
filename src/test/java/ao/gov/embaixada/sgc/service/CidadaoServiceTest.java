package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.dto.CidadaoUpdateRequest;
import ao.gov.embaixada.sgc.enums.EstadoCidadao;
import ao.gov.embaixada.sgc.enums.EstadoCivil;
import ao.gov.embaixada.sgc.enums.Sexo;
import ao.gov.embaixada.sgc.exception.DuplicateResourceException;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CidadaoServiceTest {

    @Autowired
    private CidadaoService cidadaoService;

    @Test
    void shouldCreateCidadao() {
        CidadaoCreateRequest request = new CidadaoCreateRequest(
                "N1234567", "Joao Silva", LocalDate.of(1990, 1, 15),
                Sexo.MASCULINO, "Angolana", EstadoCivil.SOLTEIRO,
                "joao@email.com", "+49123456", "Luanda", "Berlin");

        CidadaoResponse response = cidadaoService.create(request);

        assertNotNull(response.id());
        assertEquals("N1234567", response.numeroPassaporte());
        assertEquals("Joao Silva", response.nomeCompleto());
        assertEquals(EstadoCidadao.ACTIVO, response.estado());
    }

    @Test
    void shouldRejectDuplicatePassport() {
        CidadaoCreateRequest request = new CidadaoCreateRequest(
                "DUP001", "Maria Santos", LocalDate.of(1985, 5, 20),
                Sexo.FEMININO, "Angolana", EstadoCivil.CASADO,
                null, null, null, null);

        cidadaoService.create(request);

        assertThrows(DuplicateResourceException.class, () -> cidadaoService.create(request));
    }

    @Test
    void shouldFindById() {
        CidadaoCreateRequest request = new CidadaoCreateRequest(
                "FIND001", "Pedro Costa", null,
                null, "Angolana", null,
                null, null, null, null);

        CidadaoResponse created = cidadaoService.create(request);
        CidadaoResponse found = cidadaoService.findById(created.id());

        assertEquals(created.id(), found.id());
        assertEquals("Pedro Costa", found.nomeCompleto());
    }

    @Test
    void shouldThrowNotFoundForInvalidId() {
        UUID randomId = UUID.randomUUID();
        assertThrows(ResourceNotFoundException.class, () -> cidadaoService.findById(randomId));
    }

    @Test
    void shouldUpdateCidadao() {
        CidadaoCreateRequest createReq = new CidadaoCreateRequest(
                "UPD001", "Ana Ferreira", null,
                Sexo.FEMININO, "Angolana", null,
                null, null, null, null);

        CidadaoResponse created = cidadaoService.create(createReq);

        CidadaoUpdateRequest updateReq = new CidadaoUpdateRequest(
                "Ana Maria Ferreira", LocalDate.of(1992, 3, 10),
                Sexo.FEMININO, "Angolana", EstadoCivil.CASADO,
                "ana@email.com", "+49789012", "Benguela", "Hamburg");

        CidadaoResponse updated = cidadaoService.update(created.id(), updateReq);

        assertEquals("Ana Maria Ferreira", updated.nomeCompleto());
        assertEquals("ana@email.com", updated.email());
    }

    @Test
    void shouldUpdateEstado() {
        CidadaoCreateRequest request = new CidadaoCreateRequest(
                "EST001", "Carlos Neto", null,
                null, "Angolana", null,
                null, null, null, null);

        CidadaoResponse created = cidadaoService.create(request);
        assertEquals(EstadoCidadao.ACTIVO, created.estado());

        CidadaoResponse updated = cidadaoService.updateEstado(created.id(), EstadoCidadao.INACTIVO);
        assertEquals(EstadoCidadao.INACTIVO, updated.estado());
    }
}
