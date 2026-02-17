package ao.gov.embaixada.sgc.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "notification_preferences",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_pref_cidadao_workflow",
                columnNames = {"cidadao_id", "workflow_name"}))
public class NotificationPreference extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidadao_id", nullable = false)
    private Cidadao cidadao;

    @Column(name = "workflow_name", nullable = false, length = 50)
    private String workflowName;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    public Cidadao getCidadao() { return cidadao; }
    public void setCidadao(Cidadao cidadao) { this.cidadao = cidadao; }

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }

    public boolean isEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }
}
