package ao.gov.embaixada.sgc.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NotificationPreferenceUpdateRequest(
        @NotEmpty @Valid List<WorkflowPreference> preferences
) {
    public record WorkflowPreference(
            @NotNull String workflowName,
            @NotNull Boolean emailEnabled
    ) {}
}
