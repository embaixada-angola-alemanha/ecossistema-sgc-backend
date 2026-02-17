package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.AbstractIntegrationTest;
import ao.gov.embaixada.sgc.dto.CidadaoCreateRequest;
import ao.gov.embaixada.sgc.dto.CidadaoResponse;
import ao.gov.embaixada.sgc.dto.NotificationPreferenceResponse;
import ao.gov.embaixada.sgc.dto.NotificationPreferenceUpdateRequest;
import ao.gov.embaixada.sgc.dto.NotificationPreferenceUpdateRequest.WorkflowPreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class NotificationPreferenceServiceTest extends AbstractIntegrationTest {

    @Autowired
    private NotificationPreferenceService preferenceService;

    @Autowired
    private CidadaoService cidadaoService;

    private UUID cidadaoId;

    @BeforeEach
    void setUp() {
        CidadaoCreateRequest req = new CidadaoCreateRequest(
                "NP-PREF-" + System.nanoTime(), "Test Cidadao Pref", null,
                null, "Angolana", null,
                null, null, null, null);
        CidadaoResponse cidadao = cidadaoService.create(req);
        cidadaoId = cidadao.id();
    }

    @Test
    void shouldReturnEnabledByDefaultWhenNoPreferenceExists() {
        assertTrue(preferenceService.isNotificationEnabled(cidadaoId, "Visa"));
        assertTrue(preferenceService.isNotificationEnabled(cidadaoId, "Agendamento"));
    }

    @Test
    void shouldReturnEmptyListWhenNoPreferencesSet() {
        List<NotificationPreferenceResponse> prefs = preferenceService.findByCidadaoId(cidadaoId);
        assertTrue(prefs.isEmpty());
    }

    @Test
    void shouldCreateAndUpdatePreferences() {
        var request = new NotificationPreferenceUpdateRequest(List.of(
                new WorkflowPreference("Visa", false),
                new WorkflowPreference("Agendamento", true)));

        List<NotificationPreferenceResponse> result = preferenceService.updatePreferences(cidadaoId, request);

        assertEquals(2, result.size());
        assertFalse(preferenceService.isNotificationEnabled(cidadaoId, "Visa"));
        assertTrue(preferenceService.isNotificationEnabled(cidadaoId, "Agendamento"));
    }

    @Test
    void shouldUpdateExistingPreference() {
        // First set to disabled
        preferenceService.updatePreferences(cidadaoId, new NotificationPreferenceUpdateRequest(
                List.of(new WorkflowPreference("Visa", false))));
        assertFalse(preferenceService.isNotificationEnabled(cidadaoId, "Visa"));

        // Then re-enable
        preferenceService.updatePreferences(cidadaoId, new NotificationPreferenceUpdateRequest(
                List.of(new WorkflowPreference("Visa", true))));
        assertTrue(preferenceService.isNotificationEnabled(cidadaoId, "Visa"));
    }
}
