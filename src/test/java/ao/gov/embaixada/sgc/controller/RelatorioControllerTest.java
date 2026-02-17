package ao.gov.embaixada.sgc.controller;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.entity.AuditEventEntity;
import ao.gov.embaixada.sgc.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
class RelatorioControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @BeforeEach
    void setUp() {
        auditEventRepository.deleteAll();
    }

    @Test
    void shouldReturnDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/relatorios/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGeral").exists())
                .andExpect(jsonPath("$.visas").exists())
                .andExpect(jsonPath("$.processos").exists())
                .andExpect(jsonPath("$.registosCivis").exists())
                .andExpect(jsonPath("$.servicosNotariais").exists())
                .andExpect(jsonPath("$.agendamentos").exists());
    }

    @Test
    void shouldReturnEstatisticasForModule() throws Exception {
        mockMvc.perform(get("/api/v1/relatorios/estatisticas")
                        .param("modulo", "Visa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modulo").value("Visa"))
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.porEstado").exists())
                .andExpect(jsonPath("$.porTipo").exists());
    }

    @Test
    void shouldReturnAuditEvents() throws Exception {
        AuditEventEntity event = new AuditEventEntity();
        event.setAction("CREATE");
        event.setEntityType("VisaService");
        event.setEntityId("visa-1");
        event.setTimestamp(Instant.now());
        auditEventRepository.save(event);

        mockMvc.perform(get("/api/v1/relatorios/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].action").value("CREATE"));
    }

    @Test
    void shouldExportCsvForVisa() throws Exception {
        mockMvc.perform(get("/api/v1/relatorios/export/csv/Visa"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("relatorio_visa")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }

    @Test
    void shouldExportDashboardPdf() throws Exception {
        mockMvc.perform(get("/api/v1/relatorios/export/pdf/dashboard"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("dashboard")))
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    void shouldExportModulePdf() throws Exception {
        mockMvc.perform(get("/api/v1/relatorios/export/pdf/Visa"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("relatorio_visa")))
                .andExpect(content().contentType("application/pdf"));
    }
}
