package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.VisaChecklistResponse;
import ao.gov.embaixada.sgc.enums.TipoVisto;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class VisaDocumentChecklistService {

    private final Map<TipoVisto, List<String>> checklists = new EnumMap<>(TipoVisto.class);

    public VisaDocumentChecklistService() {
        List<String> base = List.of(
                "Passaporte válido (mínimo 6 meses)",
                "Foto 3x4 recente",
                "Formulário de pedido preenchido"
        );

        checklists.put(TipoVisto.TURISTA, concat(base, List.of(
                "Comprovante de alojamento",
                "Comprovante de meios financeiros",
                "Seguro de viagem"
        )));

        checklists.put(TipoVisto.NEGOCIO, concat(base, List.of(
                "Carta convite da empresa angolana",
                "Registo comercial da empresa",
                "Comprovante de meios financeiros",
                "Seguro de viagem"
        )));

        checklists.put(TipoVisto.TRABALHO, concat(base, List.of(
                "Contrato de trabalho",
                "Carta da empresa angolana",
                "Certificado de habilitações",
                "Registo criminal",
                "Atestado médico"
        )));

        checklists.put(TipoVisto.ESTUDANTE, concat(base, List.of(
                "Carta de aceitação da universidade",
                "Comprovante de matrícula",
                "Comprovante de meios financeiros",
                "Certificado de habilitações"
        )));

        checklists.put(TipoVisto.TRANSITO, concat(base, List.of(
                "Bilhete de viagem (ida e volta)",
                "Visto do país de destino"
        )));

        checklists.put(TipoVisto.FAMILIAR, concat(base, List.of(
                "Certidão de casamento ou nascimento",
                "Documento de identidade do familiar angolano",
                "Comprovante de residência do familiar"
        )));

        checklists.put(TipoVisto.DIPLOMATICO, concat(base, List.of(
                "Nota verbal do Ministério dos Negócios Estrangeiros",
                "Passaporte diplomático"
        )));

        checklists.put(TipoVisto.CORTESIA, concat(base, List.of(
                "Nota verbal da entidade solicitante",
                "Convite oficial"
        )));
    }

    public List<String> getRequiredDocuments(TipoVisto tipo) {
        return checklists.getOrDefault(tipo, List.of());
    }

    public VisaChecklistResponse getChecklistResponse(TipoVisto tipo) {
        return new VisaChecklistResponse(tipo, getRequiredDocuments(tipo));
    }

    private static List<String> concat(List<String> base, List<String> extra) {
        var result = new java.util.ArrayList<>(base);
        result.addAll(extra);
        return List.copyOf(result);
    }
}
