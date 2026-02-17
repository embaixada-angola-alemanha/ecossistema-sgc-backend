package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.NotificationPreferenceResponse;
import ao.gov.embaixada.sgc.dto.NotificationPreferenceUpdateRequest;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.entity.NotificationPreference;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import ao.gov.embaixada.sgc.repository.CidadaoRepository;
import ao.gov.embaixada.sgc.repository.NotificationPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final CidadaoRepository cidadaoRepository;

    public NotificationPreferenceService(NotificationPreferenceRepository preferenceRepository,
                                          CidadaoRepository cidadaoRepository) {
        this.preferenceRepository = preferenceRepository;
        this.cidadaoRepository = cidadaoRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> findByCidadaoId(UUID cidadaoId) {
        return preferenceRepository.findByCidadaoId(cidadaoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationPreferenceResponse> updatePreferences(UUID cidadaoId,
                                                                    NotificationPreferenceUpdateRequest request) {
        Cidadao cidadao = cidadaoRepository.findById(cidadaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Cidadao", cidadaoId));

        for (var pref : request.preferences()) {
            NotificationPreference entity = preferenceRepository
                    .findByCidadaoIdAndWorkflowName(cidadaoId, pref.workflowName())
                    .orElseGet(() -> {
                        NotificationPreference np = new NotificationPreference();
                        np.setCidadao(cidadao);
                        np.setWorkflowName(pref.workflowName());
                        return np;
                    });
            entity.setEmailEnabled(pref.emailEnabled());
            preferenceRepository.save(entity);
        }

        return findByCidadaoId(cidadaoId);
    }

    @Transactional(readOnly = true)
    public boolean isNotificationEnabled(UUID cidadaoId, String workflowName) {
        return preferenceRepository.findByCidadaoIdAndWorkflowName(cidadaoId, workflowName)
                .map(NotificationPreference::isEmailEnabled)
                .orElse(true); // opt-out model: enabled by default
    }

    private NotificationPreferenceResponse toResponse(NotificationPreference entity) {
        return new NotificationPreferenceResponse(
                entity.getId(),
                entity.getCidadao().getId(),
                entity.getWorkflowName(),
                entity.isEmailEnabled());
    }
}
