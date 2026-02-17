package ao.gov.embaixada.sgc.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.sgc.enums.EstadoNotificacao;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_logs")
public class NotificationLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidadao_id")
    private Cidadao cidadao;

    @Column(name = "to_address", nullable = false)
    private String toAddress;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 100)
    private String template;

    @Column(name = "workflow_name", nullable = false, length = 50)
    private String workflowName;

    @Column(name = "entity_id")
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoNotificacao estado = EstadoNotificacao.PENDENTE;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "sent_at")
    private Instant sentAt;

    public Cidadao getCidadao() { return cidadao; }
    public void setCidadao(Cidadao cidadao) { this.cidadao = cidadao; }

    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }

    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }

    public EstadoNotificacao getEstado() { return estado; }
    public void setEstado(EstadoNotificacao estado) { this.estado = estado; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
}
