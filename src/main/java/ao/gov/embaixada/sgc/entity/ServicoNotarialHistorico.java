package ao.gov.embaixada.sgc.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.sgc.enums.EstadoServicoNotarial;
import jakarta.persistence.*;

@Entity
@Table(name = "servico_notarial_historico")
public class ServicoNotarialHistorico extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_notarial_id", nullable = false)
    private ServicoNotarial servicoNotarial;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 50)
    private EstadoServicoNotarial estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_novo", nullable = false, length = 50)
    private EstadoServicoNotarial estadoNovo;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "alterado_por", length = 100)
    private String alteradoPor;

    public ServicoNotarial getServicoNotarial() { return servicoNotarial; }
    public void setServicoNotarial(ServicoNotarial servicoNotarial) { this.servicoNotarial = servicoNotarial; }

    public EstadoServicoNotarial getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoServicoNotarial estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public EstadoServicoNotarial getEstadoNovo() { return estadoNovo; }
    public void setEstadoNovo(EstadoServicoNotarial estadoNovo) { this.estadoNovo = estadoNovo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getAlteradoPor() { return alteradoPor; }
    public void setAlteradoPor(String alteradoPor) { this.alteradoPor = alteradoPor; }
}
