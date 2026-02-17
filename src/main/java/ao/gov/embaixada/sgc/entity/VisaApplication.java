package ao.gov.embaixada.sgc.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.sgc.enums.EstadoVisto;
import ao.gov.embaixada.sgc.enums.TipoVisto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "visas")
public class VisaApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidadao_id", nullable = false)
    @NotNull
    private Cidadao cidadao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull
    private TipoVisto tipo;

    @Column(name = "numero_visto", unique = true, nullable = false, length = 50)
    private String numeroVisto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull
    private EstadoVisto estado;

    @Column(name = "nacionalidade_passaporte", length = 100)
    private String nacionalidadePassaporte;

    @Column(name = "motivo_viagem", columnDefinition = "TEXT")
    private String motivoViagem;

    @Column(name = "data_entrada")
    private LocalDate dataEntrada;

    @Column(name = "data_saida")
    private LocalDate dataSaida;

    @Column(name = "local_alojamento", columnDefinition = "TEXT")
    private String localAlojamento;

    @Column(name = "entidade_convite")
    private String entidadeConvite;

    @Column(length = 100)
    private String responsavel;

    @Column(name = "valor_taxa", precision = 10, scale = 2)
    private BigDecimal valorTaxa;

    @Column(name = "taxa_paga")
    private boolean taxaPaga;

    @Column(name = "data_submissao")
    private LocalDateTime dataSubmissao;

    @Column(name = "data_decisao")
    private LocalDateTime dataDecisao;

    @Column(name = "motivo_rejeicao", columnDefinition = "TEXT")
    private String motivoRejeicao;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @ManyToMany
    @JoinTable(
            name = "visa_documentos",
            joinColumns = @JoinColumn(name = "visa_id"),
            inverseJoinColumns = @JoinColumn(name = "documento_id")
    )
    private Set<Documento> documentos = new HashSet<>();

    @OneToMany(mappedBy = "visaApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<VisaHistorico> historico;

    // Getters and Setters

    public Cidadao getCidadao() { return cidadao; }
    public void setCidadao(Cidadao cidadao) { this.cidadao = cidadao; }

    public TipoVisto getTipo() { return tipo; }
    public void setTipo(TipoVisto tipo) { this.tipo = tipo; }

    public String getNumeroVisto() { return numeroVisto; }
    public void setNumeroVisto(String numeroVisto) { this.numeroVisto = numeroVisto; }

    public EstadoVisto getEstado() { return estado; }
    public void setEstado(EstadoVisto estado) { this.estado = estado; }

    public String getNacionalidadePassaporte() { return nacionalidadePassaporte; }
    public void setNacionalidadePassaporte(String nacionalidadePassaporte) { this.nacionalidadePassaporte = nacionalidadePassaporte; }

    public String getMotivoViagem() { return motivoViagem; }
    public void setMotivoViagem(String motivoViagem) { this.motivoViagem = motivoViagem; }

    public LocalDate getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(LocalDate dataEntrada) { this.dataEntrada = dataEntrada; }

    public LocalDate getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDate dataSaida) { this.dataSaida = dataSaida; }

    public String getLocalAlojamento() { return localAlojamento; }
    public void setLocalAlojamento(String localAlojamento) { this.localAlojamento = localAlojamento; }

    public String getEntidadeConvite() { return entidadeConvite; }
    public void setEntidadeConvite(String entidadeConvite) { this.entidadeConvite = entidadeConvite; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public BigDecimal getValorTaxa() { return valorTaxa; }
    public void setValorTaxa(BigDecimal valorTaxa) { this.valorTaxa = valorTaxa; }

    public boolean isTaxaPaga() { return taxaPaga; }
    public void setTaxaPaga(boolean taxaPaga) { this.taxaPaga = taxaPaga; }

    public LocalDateTime getDataSubmissao() { return dataSubmissao; }
    public void setDataSubmissao(LocalDateTime dataSubmissao) { this.dataSubmissao = dataSubmissao; }

    public LocalDateTime getDataDecisao() { return dataDecisao; }
    public void setDataDecisao(LocalDateTime dataDecisao) { this.dataDecisao = dataDecisao; }

    public String getMotivoRejeicao() { return motivoRejeicao; }
    public void setMotivoRejeicao(String motivoRejeicao) { this.motivoRejeicao = motivoRejeicao; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Set<Documento> getDocumentos() { return documentos; }
    public void setDocumentos(Set<Documento> documentos) { this.documentos = documentos; }

    public List<VisaHistorico> getHistorico() { return historico; }
    public void setHistorico(List<VisaHistorico> historico) { this.historico = historico; }
}
