package ao.gov.embaixada.sgc.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.sgc.enums.EstadoProcesso;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "processo_historico")
public class ProcessoHistorico extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id", nullable = false)
    @NotNull
    private Processo processo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 30)
    private EstadoProcesso estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_novo", nullable = false, length = 30)
    @NotNull
    private EstadoProcesso estadoNovo;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "alterado_por")
    private String alteradoPor;

    public Processo getProcesso() { return processo; }
    public void setProcesso(Processo processo) { this.processo = processo; }

    public EstadoProcesso getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoProcesso estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public EstadoProcesso getEstadoNovo() { return estadoNovo; }
    public void setEstadoNovo(EstadoProcesso estadoNovo) { this.estadoNovo = estadoNovo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getAlteradoPor() { return alteradoPor; }
    public void setAlteradoPor(String alteradoPor) { this.alteradoPor = alteradoPor; }
}
