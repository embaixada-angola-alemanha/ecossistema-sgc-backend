package ao.gov.embaixada.sgc.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.sgc.enums.EstadoRegistoCivil;
import jakarta.persistence.*;

@Entity
@Table(name = "registo_civil_historico")
public class RegistoCivilHistorico extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registo_civil_id", nullable = false)
    private RegistoCivil registoCivil;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 50)
    private EstadoRegistoCivil estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_novo", nullable = false, length = 50)
    private EstadoRegistoCivil estadoNovo;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "alterado_por", length = 100)
    private String alteradoPor;

    public RegistoCivil getRegistoCivil() { return registoCivil; }
    public void setRegistoCivil(RegistoCivil registoCivil) { this.registoCivil = registoCivil; }

    public EstadoRegistoCivil getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoRegistoCivil estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public EstadoRegistoCivil getEstadoNovo() { return estadoNovo; }
    public void setEstadoNovo(EstadoRegistoCivil estadoNovo) { this.estadoNovo = estadoNovo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getAlteradoPor() { return alteradoPor; }
    public void setAlteradoPor(String alteradoPor) { this.alteradoPor = alteradoPor; }
}
