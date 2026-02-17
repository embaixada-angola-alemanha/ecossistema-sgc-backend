package ao.gov.embaixada.sgc.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.sgc.enums.EstadoRegistoCivil;
import ao.gov.embaixada.sgc.enums.TipoRegistoCivil;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "registos_civis")
public class RegistoCivil extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidadao_id", nullable = false)
    @NotNull
    private Cidadao cidadao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull
    private TipoRegistoCivil tipo;

    @Column(name = "numero_registo", unique = true, nullable = false, length = 50)
    private String numeroRegisto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull
    private EstadoRegistoCivil estado;

    @Column(name = "data_evento")
    private LocalDate dataEvento;

    @Column(name = "local_evento")
    private String localEvento;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(length = 100)
    private String responsavel;

    @Column(name = "motivo_rejeicao", columnDefinition = "TEXT")
    private String motivoRejeicao;

    @Column(name = "data_submissao")
    private LocalDateTime dataSubmissao;

    @Column(name = "data_verificacao")
    private LocalDateTime dataVerificacao;

    @Column(name = "data_certificado")
    private LocalDateTime dataCertificado;

    // Birth-specific
    @Column(name = "nome_pai")
    private String nomePai;

    @Column(name = "nome_mae")
    private String nomeMae;

    @Column(name = "local_nascimento")
    private String localNascimento;

    // Marriage-specific
    @Column(name = "nome_conjuge1")
    private String nomeConjuge1;

    @Column(name = "nome_conjuge2")
    private String nomeConjuge2;

    @Column(name = "regime_casamento", length = 100)
    private String regimeCasamento;

    // Death-specific
    @Column(name = "causa_obito", columnDefinition = "TEXT")
    private String causaObito;

    @Column(name = "local_obito")
    private String localObito;

    @Column(name = "data_obito")
    private LocalDate dataObito;

    // Certificate storage
    @Column(name = "certificado_object_key", length = 500)
    private String certificadoObjectKey;

    @Column(name = "certificado_url", length = 500)
    private String certificadoUrl;

    @ManyToMany
    @JoinTable(
            name = "registo_civil_documentos",
            joinColumns = @JoinColumn(name = "registo_civil_id"),
            inverseJoinColumns = @JoinColumn(name = "documento_id")
    )
    private Set<Documento> documentos = new HashSet<>();

    @OneToMany(mappedBy = "registoCivil", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<RegistoCivilHistorico> historico;

    // Getters and Setters

    public Cidadao getCidadao() { return cidadao; }
    public void setCidadao(Cidadao cidadao) { this.cidadao = cidadao; }

    public TipoRegistoCivil getTipo() { return tipo; }
    public void setTipo(TipoRegistoCivil tipo) { this.tipo = tipo; }

    public String getNumeroRegisto() { return numeroRegisto; }
    public void setNumeroRegisto(String numeroRegisto) { this.numeroRegisto = numeroRegisto; }

    public EstadoRegistoCivil getEstado() { return estado; }
    public void setEstado(EstadoRegistoCivil estado) { this.estado = estado; }

    public LocalDate getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDate dataEvento) { this.dataEvento = dataEvento; }

    public String getLocalEvento() { return localEvento; }
    public void setLocalEvento(String localEvento) { this.localEvento = localEvento; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getMotivoRejeicao() { return motivoRejeicao; }
    public void setMotivoRejeicao(String motivoRejeicao) { this.motivoRejeicao = motivoRejeicao; }

    public LocalDateTime getDataSubmissao() { return dataSubmissao; }
    public void setDataSubmissao(LocalDateTime dataSubmissao) { this.dataSubmissao = dataSubmissao; }

    public LocalDateTime getDataVerificacao() { return dataVerificacao; }
    public void setDataVerificacao(LocalDateTime dataVerificacao) { this.dataVerificacao = dataVerificacao; }

    public LocalDateTime getDataCertificado() { return dataCertificado; }
    public void setDataCertificado(LocalDateTime dataCertificado) { this.dataCertificado = dataCertificado; }

    public String getNomePai() { return nomePai; }
    public void setNomePai(String nomePai) { this.nomePai = nomePai; }

    public String getNomeMae() { return nomeMae; }
    public void setNomeMae(String nomeMae) { this.nomeMae = nomeMae; }

    public String getLocalNascimento() { return localNascimento; }
    public void setLocalNascimento(String localNascimento) { this.localNascimento = localNascimento; }

    public String getNomeConjuge1() { return nomeConjuge1; }
    public void setNomeConjuge1(String nomeConjuge1) { this.nomeConjuge1 = nomeConjuge1; }

    public String getNomeConjuge2() { return nomeConjuge2; }
    public void setNomeConjuge2(String nomeConjuge2) { this.nomeConjuge2 = nomeConjuge2; }

    public String getRegimeCasamento() { return regimeCasamento; }
    public void setRegimeCasamento(String regimeCasamento) { this.regimeCasamento = regimeCasamento; }

    public String getCausaObito() { return causaObito; }
    public void setCausaObito(String causaObito) { this.causaObito = causaObito; }

    public String getLocalObito() { return localObito; }
    public void setLocalObito(String localObito) { this.localObito = localObito; }

    public LocalDate getDataObito() { return dataObito; }
    public void setDataObito(LocalDate dataObito) { this.dataObito = dataObito; }

    public String getCertificadoObjectKey() { return certificadoObjectKey; }
    public void setCertificadoObjectKey(String certificadoObjectKey) { this.certificadoObjectKey = certificadoObjectKey; }

    public String getCertificadoUrl() { return certificadoUrl; }
    public void setCertificadoUrl(String certificadoUrl) { this.certificadoUrl = certificadoUrl; }

    public Set<Documento> getDocumentos() { return documentos; }
    public void setDocumentos(Set<Documento> documentos) { this.documentos = documentos; }

    public List<RegistoCivilHistorico> getHistorico() { return historico; }
    public void setHistorico(List<RegistoCivilHistorico> historico) { this.historico = historico; }
}
