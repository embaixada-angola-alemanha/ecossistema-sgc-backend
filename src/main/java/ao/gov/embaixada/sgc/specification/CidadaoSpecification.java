package ao.gov.embaixada.sgc.specification;

import ao.gov.embaixada.sgc.entity.Cidadao;
import ao.gov.embaixada.sgc.enums.EstadoCidadao;
import ao.gov.embaixada.sgc.enums.Sexo;
import org.springframework.data.jpa.domain.Specification;

public final class CidadaoSpecification {

    private CidadaoSpecification() {}

    public static Specification<Cidadao> withNome(String nome) {
        if (nome == null || nome.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("nomeCompleto")), "%" + nome.toLowerCase() + "%");
    }

    public static Specification<Cidadao> withEstado(EstadoCidadao estado) {
        if (estado == null) return null;
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    public static Specification<Cidadao> withNacionalidade(String nacionalidade) {
        if (nacionalidade == null || nacionalidade.isBlank()) return null;
        return (root, query, cb) -> cb.equal(root.get("nacionalidade"), nacionalidade);
    }

    public static Specification<Cidadao> withSexo(Sexo sexo) {
        if (sexo == null) return null;
        return (root, query, cb) -> cb.equal(root.get("sexo"), sexo);
    }
}
