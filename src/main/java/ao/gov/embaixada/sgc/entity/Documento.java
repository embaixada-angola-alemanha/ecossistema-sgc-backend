package ao.gov.embaixada.sgc.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.sgc.enums.EstadoDocumento;
import ao.gov.embaixada.sgc.enums.TipoDocumento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "documentos")
public class Documento extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cidadao_id", nullable = false)
    @NotNull
    private Cidadao cidadao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull
    private TipoDocumento tipo;

    @Column(length = 100)
    private String numero;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(name = "ficheiro_url", length = 500)
    private String ficheiroUrl;

    @Column(name = "ficheiro_nome")
    private String ficheiroNome;

    @Column(name = "ficheiro_tamanho")
    private Long ficheiroTamanho;

    @Column(name = "ficheiro_tipo", length = 100)
    private String ficheiroTipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoDocumento estado = EstadoDocumento.PENDENTE;

    @Column(name = "versao", nullable = false)
    private Integer versao = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_original_id")
    private Documento documentoOriginal;

    @Column(name = "ficheiro_object_key", length = 500)
    private String ficheiroObjectKey;

    public Cidadao getCidadao() { return cidadao; }
    public void setCidadao(Cidadao cidadao) { this.cidadao = cidadao; }

    public TipoDocumento getTipo() { return tipo; }
    public void setTipo(TipoDocumento tipo) { this.tipo = tipo; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public LocalDate getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDate dataEmissao) { this.dataEmissao = dataEmissao; }

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }

    public String getFicheiroUrl() { return ficheiroUrl; }
    public void setFicheiroUrl(String ficheiroUrl) { this.ficheiroUrl = ficheiroUrl; }

    public String getFicheiroNome() { return ficheiroNome; }
    public void setFicheiroNome(String ficheiroNome) { this.ficheiroNome = ficheiroNome; }

    public Long getFicheiroTamanho() { return ficheiroTamanho; }
    public void setFicheiroTamanho(Long ficheiroTamanho) { this.ficheiroTamanho = ficheiroTamanho; }

    public String getFicheiroTipo() { return ficheiroTipo; }
    public void setFicheiroTipo(String ficheiroTipo) { this.ficheiroTipo = ficheiroTipo; }

    public EstadoDocumento getEstado() { return estado; }
    public void setEstado(EstadoDocumento estado) { this.estado = estado; }

    public Integer getVersao() { return versao; }
    public void setVersao(Integer versao) { this.versao = versao; }

    public Documento getDocumentoOriginal() { return documentoOriginal; }
    public void setDocumentoOriginal(Documento documentoOriginal) { this.documentoOriginal = documentoOriginal; }

    public String getFicheiroObjectKey() { return ficheiroObjectKey; }
    public void setFicheiroObjectKey(String ficheiroObjectKey) { this.ficheiroObjectKey = ficheiroObjectKey; }
}
