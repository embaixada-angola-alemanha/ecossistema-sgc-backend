package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.commons.security.EcossistemaRole;
import ao.gov.embaixada.commons.security.SecurityUtils;
import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.exception.ResourceNotFoundException;
import ao.gov.embaixada.sgc.repository.CidadaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CitizenContextService {

    private static final EcossistemaRole[] STAFF_ROLES = {
            EcossistemaRole.ADMIN, EcossistemaRole.CONSUL,
            EcossistemaRole.OFFICER, EcossistemaRole.EDITOR, EcossistemaRole.VIEWER
    };

    private final CidadaoRepository cidadaoRepository;

    public CitizenContextService(CidadaoRepository cidadaoRepository) {
        this.cidadaoRepository = cidadaoRepository;
    }

    public boolean isCitizenOnly() {
        if (!SecurityUtils.hasRole(EcossistemaRole.CITIZEN)) {
            return false;
        }
        for (EcossistemaRole role : STAFF_ROLES) {
            if (SecurityUtils.hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    public Optional<Cidadao> getCurrentCidadao() {
        return SecurityUtils.getCurrentUserSubject()
                .flatMap(cidadaoRepository::findByKeycloakId);
    }

    public UUID requireCurrentCidadaoId() {
        return getCurrentCidadao()
                .map(Cidadao::getId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cidadao", "keycloakId", SecurityUtils.getCurrentUserSubject().orElse("unknown")));
    }

    public boolean canAccessCidadaoData(UUID cidadaoId) {
        if (!isCitizenOnly()) {
            return true;
        }
        return getCurrentCidadao()
                .map(c -> c.getId().equals(cidadaoId))
                .orElse(false);
    }
}
