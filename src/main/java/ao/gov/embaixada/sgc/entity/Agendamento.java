package ao.gov.embaixada.sgc.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import ao.gov.embaixada.sgc.enums.TipoAgendamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "agendamentos")
public class Agendamento extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidadao_id", nullable = false)
    @NotNull
    private Cidadao cidadao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull
    private TipoAgendamento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull
    private EstadoAgendamento estado;

    @Column(name = "numero_agendamento", unique = true, nullable = false, length = 50)
    private String numeroAgendamento;

    @Column(name = "data_hora", nullable = false)
    @NotNull
    private LocalDateTime dataHora;

    @Column(name = "duracao_minutos", nullable = false)
    private int duracaoMinutos = 30;

    @Column(length = 255)
    private String local = "Embaixada de Angola — Berlim";

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    @OneToMany(mappedBy = "agendamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<AgendamentoHistorico> historico;

    // Getters and Setters

    public Cidadao getCidadao() { return cidadao; }
    public void setCidadao(Cidadao cidadao) { this.cidadao = cidadao; }

    public TipoAgendamento getTipo() { return tipo; }
    public void setTipo(TipoAgendamento tipo) { this.tipo = tipo; }

    public EstadoAgendamento getEstado() { return estado; }
    public void setEstado(EstadoAgendamento estado) { this.estado = estado; }

    public String getNumeroAgendamento() { return numeroAgendamento; }
    public void setNumeroAgendamento(String numeroAgendamento) { this.numeroAgendamento = numeroAgendamento; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public int getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(int duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public String getMotivoCancelamento() { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }

    public List<AgendamentoHistorico> getHistorico() { return historico; }
    public void setHistorico(List<AgendamentoHistorico> historico) { this.historico = historico; }
}
