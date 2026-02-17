package ao.gov.embaixada.sgc.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.sgc.enums.EstadoVisto;
import jakarta.persistence.*;

@Entity
@Table(name = "visa_historico")
public class VisaHistorico extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visa_id", nullable = false)
    private VisaApplication visaApplication;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 50)
    private EstadoVisto estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_novo", nullable = false, length = 50)
    private EstadoVisto estadoNovo;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "alterado_por", length = 100)
    private String alteradoPor;

    public VisaApplication getVisaApplication() { return visaApplication; }
    public void setVisaApplication(VisaApplication visaApplication) { this.visaApplication = visaApplication; }

    public EstadoVisto getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoVisto estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public EstadoVisto getEstadoNovo() { return estadoNovo; }
    public void setEstadoNovo(EstadoVisto estadoNovo) { this.estadoNovo = estadoNovo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getAlteradoPor() { return alteradoPor; }
    public void setAlteradoPor(String alteradoPor) { this.alteradoPor = alteradoPor; }
}
