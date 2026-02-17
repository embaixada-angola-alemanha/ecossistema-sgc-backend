package ao.gov.embaixada.sgc.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.sgc.enums.EstadoServicoNotarial;
import ao.gov.embaixada.sgc.enums.TipoServicoNotarial;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "servicos_notariais")
public class ServicoNotarial extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidadao_id", nullable = false)
    @NotNull
    private Cidadao cidadao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull
    private TipoServicoNotarial tipo;

    @Column(name = "numero_servico", unique = true, nullable = false, length = 50)
    private String numeroServico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull
    private EstadoServicoNotarial estado;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(length = 100)
    private String responsavel;

    @Column(name = "motivo_rejeicao", columnDefinition = "TEXT")
    private String motivoRejeicao;

    @Column(name = "valor_taxa", precision = 10, scale = 2)
    private BigDecimal valorTaxa;

    @Column(name = "taxa_paga")
    private boolean taxaPaga;

    @Column(name = "data_submissao")
    private LocalDateTime dataSubmissao;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    // Power of attorney (PROCURACAO)
    @Column(name = "outorgante")
    private String outorgante;

    @Column(name = "outorgado")
    private String outorgado;

    @Column(name = "finalidade_procuracao", columnDefinition = "TEXT")
    private String finalidadeProcuracao;

    // Legalization (LEGALIZACAO)
    @Column(name = "documento_origem")
    private String documentoOrigem;

    @Column(name = "pais_origem", length = 100)
    private String paisOrigem;

    @Column(name = "entidade_emissora")
    private String entidadeEmissora;

    // Apostille (APOSTILA)
    @Column(name = "documento_apostilado")
    private String documentoApostilado;

    @Column(name = "pais_destino", length = 100)
    private String paisDestino;

    // Certified copy (COPIA_CERTIFICADA)
    @Column(name = "documento_original_ref")
    private String documentoOriginalRef;

    @Column(name = "numero_copias")
    private Integer numeroCopias;

    // Certificate storage
    @Column(name = "certificado_object_key", length = 500)
    private String certificadoObjectKey;

    @Column(name = "certificado_url", length = 500)
    private String certificadoUrl;

    // Appointment link
    @Column(name = "agendamento_id")
    private UUID agendamentoId;

    @ManyToMany
    @JoinTable(
            name = "servico_notarial_documentos",
            joinColumns = @JoinColumn(name = "servico_notarial_id"),
            inverseJoinColumns = @JoinColumn(name = "documento_id")
    )
    private Set<Documento> documentos = new HashSet<>();

    @OneToMany(mappedBy = "servicoNotarial", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<ServicoNotarialHistorico> historico;

    // Getters and Setters

    public Cidadao getCidadao() { return cidadao; }
    public void setCidadao(Cidadao cidadao) { this.cidadao = cidadao; }

    public TipoServicoNotarial getTipo() { return tipo; }
    public void setTipo(TipoServicoNotarial tipo) { this.tipo = tipo; }

    public String getNumeroServico() { return numeroServico; }
    public void setNumeroServico(String numeroServico) { this.numeroServico = numeroServico; }

    public EstadoServicoNotarial getEstado() { return estado; }
    public void setEstado(EstadoServicoNotarial estado) { this.estado = estado; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getMotivoRejeicao() { return motivoRejeicao; }
    public void setMotivoRejeicao(String motivoRejeicao) { this.motivoRejeicao = motivoRejeicao; }

    public BigDecimal getValorTaxa() { return valorTaxa; }
    public void setValorTaxa(BigDecimal valorTaxa) { this.valorTaxa = valorTaxa; }

    public boolean isTaxaPaga() { return taxaPaga; }
    public void setTaxaPaga(boolean taxaPaga) { this.taxaPaga = taxaPaga; }

    public LocalDateTime getDataSubmissao() { return dataSubmissao; }
    public void setDataSubmissao(LocalDateTime dataSubmissao) { this.dataSubmissao = dataSubmissao; }

    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDateTime dataConclusao) { this.dataConclusao = dataConclusao; }

    public String getOutorgante() { return outorgante; }
    public void setOutorgante(String outorgante) { this.outorgante = outorgante; }

    public String getOutorgado() { return outorgado; }
    public void setOutorgado(String outorgado) { this.outorgado = outorgado; }

    public String getFinalidadeProcuracao() { return finalidadeProcuracao; }
    public void setFinalidadeProcuracao(String finalidadeProcuracao) { this.finalidadeProcuracao = finalidadeProcuracao; }

    public String getDocumentoOrigem() { return documentoOrigem; }
    public void setDocumentoOrigem(String documentoOrigem) { this.documentoOrigem = documentoOrigem; }

    public String getPaisOrigem() { return paisOrigem; }
    public void setPaisOrigem(String paisOrigem) { this.paisOrigem = paisOrigem; }

    public String getEntidadeEmissora() { return entidadeEmissora; }
    public void setEntidadeEmissora(String entidadeEmissora) { this.entidadeEmissora = entidadeEmissora; }

    public String getDocumentoApostilado() { return documentoApostilado; }
    public void setDocumentoApostilado(String documentoApostilado) { this.documentoApostilado = documentoApostilado; }

    public String getPaisDestino() { return paisDestino; }
    public void setPaisDestino(String paisDestino) { this.paisDestino = paisDestino; }

    public String getDocumentoOriginalRef() { return documentoOriginalRef; }
    public void setDocumentoOriginalRef(String documentoOriginalRef) { this.documentoOriginalRef = documentoOriginalRef; }

    public Integer getNumeroCopias() { return numeroCopias; }
    public void setNumeroCopias(Integer numeroCopias) { this.numeroCopias = numeroCopias; }

    public String getCertificadoObjectKey() { return certificadoObjectKey; }
    public void setCertificadoObjectKey(String certificadoObjectKey) { this.certificadoObjectKey = certificadoObjectKey; }

    public String getCertificadoUrl() { return certificadoUrl; }
    public void setCertificadoUrl(String certificadoUrl) { this.certificadoUrl = certificadoUrl; }

    public UUID getAgendamentoId() { return agendamentoId; }
    public void setAgendamentoId(UUID agendamentoId) { this.agendamentoId = agendamentoId; }

    public Set<Documento> getDocumentos() { return documentos; }
    public void setDocumentos(Set<Documento> documentos) { this.documentos = documentos; }

    public List<ServicoNotarialHistorico> getHistorico() { return historico; }
    public void setHistorico(List<ServicoNotarialHistorico> historico) { this.historico = historico; }
}
