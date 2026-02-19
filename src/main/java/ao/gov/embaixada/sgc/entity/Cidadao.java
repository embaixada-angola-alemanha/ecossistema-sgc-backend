package ao.gov.embaixada.sgc.entity;

import ao.gov.embaixada.commons.dto.BaseEntity;
import ao.gov.embaixada.sgc.enums.EstadoCidadao;
import ao.gov.embaixada.sgc.enums.EstadoCivil;
import ao.gov.embaixada.sgc.enums.Sexo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cidadaos")
public class Cidadao extends BaseEntity {

    @NotBlank
    @Column(name = "numero_passaporte", nullable = false, unique = true, length = 50)
    private String numeroPassaporte;

    @NotBlank
    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sexo sexo;

    @Column(length = 100)
    private String nacionalidade = "Angolana";

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", length = 30)
    private EstadoCivil estadoCivil;

    private String email;

    @Column(length = 50)
    private String telefone;

    @Column(name = "endereco_angola", columnDefinition = "TEXT")
    private String enderecoAngola;

    @Column(name = "endereco_alemanha", columnDefinition = "TEXT")
    private String enderecoAlemanha;

    @Column(name = "keycloak_id")
    private String keycloakId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoCidadao estado = EstadoCidadao.ACTIVO;

    @OneToMany(mappedBy = "cidadao", cascade = CascadeType.ALL)
    private List<Documento> documentos = new ArrayList<>();

    @OneToMany(mappedBy = "cidadao", cascade = CascadeType.ALL)
    private List<Processo> processos = new ArrayList<>();

    public String getNumeroPassaporte() { return numeroPassaporte; }
    public void setNumeroPassaporte(String numeroPassaporte) { this.numeroPassaporte = numeroPassaporte; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public String getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(String nacionalidade) { this.nacionalidade = nacionalidade; }

    public EstadoCivil getEstadoCivil() { return estadoCivil; }
    public void setEstadoCivil(EstadoCivil estadoCivil) { this.estadoCivil = estadoCivil; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEnderecoAngola() { return enderecoAngola; }
    public void setEnderecoAngola(String enderecoAngola) { this.enderecoAngola = enderecoAngola; }

    public String getEnderecoAlemanha() { return enderecoAlemanha; }
    public void setEnderecoAlemanha(String enderecoAlemanha) { this.enderecoAlemanha = enderecoAlemanha; }

    public String getKeycloakId() { return keycloakId; }
    public void setKeycloakId(String keycloakId) { this.keycloakId = keycloakId; }

    public EstadoCidadao getEstado() { return estado; }
    public void setEstado(EstadoCidadao estado) { this.estado = estado; }

    public List<Documento> getDocumentos() { return documentos; }
    public void setDocumentos(List<Documento> documentos) { this.documentos = documentos; }

    public List<Processo> getProcessos() { return processos; }
    public void setProcessos(List<Processo> processos) { this.processos = processos; }
}
