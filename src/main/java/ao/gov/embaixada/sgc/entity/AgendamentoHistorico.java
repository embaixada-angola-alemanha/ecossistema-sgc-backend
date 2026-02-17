package ao.gov.embaixada.sgc.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import jakarta.persistence.*;

@Entity
@Table(name = "agendamento_historico")
public class AgendamentoHistorico extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 50)
    private EstadoAgendamento estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_novo", nullable = false, length = 50)
    private EstadoAgendamento estadoNovo;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "alterado_por", length = 100)
    private String alteradoPor;

    public Agendamento getAgendamento() { return agendamento; }
    public void setAgendamento(Agendamento agendamento) { this.agendamento = agendamento; }

    public EstadoAgendamento getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoAgendamento estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public EstadoAgendamento getEstadoNovo() { return estadoNovo; }
    public void setEstadoNovo(EstadoAgendamento estadoNovo) { this.estadoNovo = estadoNovo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getAlteradoPor() { return alteradoPor; }
    public void setAlteradoPor(String alteradoPor) { this.alteradoPor = alteradoPor; }
}
