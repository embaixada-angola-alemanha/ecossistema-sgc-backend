package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.commons.storage.StorageService;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.entity.RegistoCivil;
import ao.gov.embaixada.sgc.enums.EstadoRegistoCivil;
import ao.gov.embaixada.sgc.enums.TipoRegistoCivil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificadoServiceTest {

    @Mock
    private StorageService storageService;

    @InjectMocks
    private CertificadoService certificadoService;

    private Cidadao cidadao;

    @BeforeEach
    void setUp() {
        cidadao = new Cidadao();
        cidadao.setNomeCompleto("Joao Silva");
    }

    private RegistoCivil buildRegisto(TipoRegistoCivil tipo) {
        RegistoCivil registo = new RegistoCivil();
        registo.setTipo(tipo);
        registo.setEstado(EstadoRegistoCivil.VERIFICADO);
        registo.setCidadao(cidadao);
        registo.setDataEvento(LocalDate.of(1990, 5, 15));
        registo.setLocalEvento("Berlin");

        switch (tipo) {
            case NASCIMENTO -> {
                registo.setNumeroRegisto("SGC-NAS-00001");
                registo.setNomePai("Pai Silva");
                registo.setNomeMae("Mae Silva");
                registo.setLocalNascimento("Luanda");
            }
            case CASAMENTO -> {
                registo.setNumeroRegisto("SGC-CAS-00001");
                registo.setNomeConjuge1("Joao Silva");
                registo.setNomeConjuge2("Ana Santos");
                registo.setRegimeCasamento("Comunhao de adquiridos");
            }
            case OBITO -> {
                registo.setNumeroRegisto("SGC-OBI-00001");
                registo.setCausaObito("Causa natural");
                registo.setLocalObito("Berlin");
                registo.setDataObito(LocalDate.of(2024, 1, 10));
            }
        }

        return registo;
    }

    @Test
    void shouldGenerateBirthCertificatePdf() {
        RegistoCivil registo = buildRegisto(TipoRegistoCivil.NASCIMENTO);

        byte[] pdf = certificadoService.generatePdf(registo);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        // PDF magic bytes: %PDF
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }

    @Test
    void shouldGenerateMarriageCertificatePdf() {
        RegistoCivil registo = buildRegisto(TipoRegistoCivil.CASAMENTO);

        byte[] pdf = certificadoService.generatePdf(registo);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        assertEquals('%', (char) pdf[0]);
    }

    @Test
    void shouldGenerateDeathCertificatePdf() {
        RegistoCivil registo = buildRegisto(TipoRegistoCivil.OBITO);

        byte[] pdf = certificadoService.generatePdf(registo);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        assertEquals('%', (char) pdf[0]);
    }

    @Test
    void shouldGenerateAndStoreReturnsObjectKey() {
        RegistoCivil registo = buildRegisto(TipoRegistoCivil.NASCIMENTO);
        UUID id = UUID.randomUUID();
        // Set ID via reflection or use a spy — for simplicity we test the key format
        when(storageService.getDefaultBucket()).thenReturn("sgc-bucket");
        when(storageService.upload(anyString(), anyString(), any(), anyLong(), anyString()))
                .thenAnswer(inv -> inv.getArgument(1));

        String objectKey = certificadoService.generateAndStore(registo);

        assertNotNull(objectKey);
        assertTrue(objectKey.startsWith("certificados/nascimento/"));
        assertTrue(objectKey.endsWith("SGC-NAS-00001.pdf"));
    }
}
