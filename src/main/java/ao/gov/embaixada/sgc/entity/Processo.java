package ao.gov.embaixada.sgc.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.sgc.enums.EstadoProcesso;
import ao.gov.embaixada.sgc.enums.Prioridade;
import ao.gov.embaixada.sgc.enums.TipoProcesso;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "processos")
public class Processo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidadao_id", nullable = false)
    @NotNull
    private Cidadao cidadao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull
    private TipoProcesso tipo;

    @NotBlank
    @Column(name = "numero_processo", nullable = false, unique = true, length = 50)
    private String numeroProcesso;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoProcesso estado = EstadoProcesso.RASCUNHO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridade prioridade = Prioridade.NORMAL;

    private String responsavel;

    @Column(name = "valor_taxa", precision = 10, scale = 2)
    private BigDecimal valorTaxa = BigDecimal.ZERO;

    @Column(name = "taxa_paga", nullable = false)
    private boolean taxaPaga = false;

    @Column(name = "data_submissao")
    private LocalDateTime dataSubmissao;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @ManyToMany
    @JoinTable(
            name = "processo_documentos",
            joinColumns = @JoinColumn(name = "processo_id"),
            inverseJoinColumns = @JoinColumn(name = "documento_id")
    )
    private Set<Documento> documentos = new HashSet<>();

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcessoHistorico> historico = new ArrayList<>();

    public Cidadao getCidadao() { return cidadao; }
    public void setCidadao(Cidadao cidadao) { this.cidadao = cidadao; }

    public TipoProcesso getTipo() { return tipo; }
    public void setTipo(TipoProcesso tipo) { this.tipo = tipo; }

    public String getNumeroProcesso() { return numeroProcesso; }
    public void setNumeroProcesso(String numeroProcesso) { this.numeroProcesso = numeroProcesso; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public EstadoProcesso getEstado() { return estado; }
    public void setEstado(EstadoProcesso estado) { this.estado = estado; }

    public Prioridade getPrioridade() { return prioridade; }
    public void setPrioridade(Prioridade prioridade) { this.prioridade = prioridade; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public BigDecimal getValorTaxa() { return valorTaxa; }
    public void setValorTaxa(BigDecimal valorTaxa) { this.valorTaxa = valorTaxa; }

    public boolean isTaxaPaga() { return taxaPaga; }
    public void setTaxaPaga(boolean taxaPaga) { this.taxaPaga = taxaPaga; }

    public LocalDateTime getDataSubmissao() { return dataSubmissao; }
    public void setDataSubmissao(LocalDateTime dataSubmissao) { this.dataSubmissao = dataSubmissao; }

    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDateTime dataConclusao) { this.dataConclusao = dataConclusao; }

    public Set<Documento> getDocumentos() { return documentos; }
    public void setDocumentos(Set<Documento> documentos) { this.documentos = documentos; }

    public List<ProcessoHistorico> getHistorico() { return historico; }
    public void setHistorico(List<ProcessoHistorico> historico) { this.historico = historico; }
}
