package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.VisaFeeResponse;
import ao.gov.embaixada.sgc.enums.TipoVisto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@Component
public class VisaFeeCalculator {

    private final Map<TipoVisto, BigDecimal> fees = new EnumMap<>(TipoVisto.class);

    public VisaFeeCalculator() {
        fees.put(TipoVisto.TURISTA, new BigDecimal("60.00"));
        fees.put(TipoVisto.NEGOCIO, new BigDecimal("100.00"));
        fees.put(TipoVisto.TRABALHO, new BigDecimal("150.00"));
        fees.put(TipoVisto.ESTUDANTE, new BigDecimal("40.00"));
        fees.put(TipoVisto.TRANSITO, new BigDecimal("30.00"));
        fees.put(TipoVisto.FAMILIAR, new BigDecimal("60.00"));
        fees.put(TipoVisto.DIPLOMATICO, BigDecimal.ZERO);
        fees.put(TipoVisto.CORTESIA, BigDecimal.ZERO);
    }

    public BigDecimal calculateFee(TipoVisto tipo) {
        return fees.getOrDefault(tipo, BigDecimal.ZERO);
    }

    public boolean isIsento(TipoVisto tipo) {
        BigDecimal fee = calculateFee(tipo);
        return fee.compareTo(BigDecimal.ZERO) == 0;
    }

    public VisaFeeResponse getFeeResponse(TipoVisto tipo) {
        BigDecimal valor = calculateFee(tipo);
        return new VisaFeeResponse(tipo, valor, "EUR", isIsento(tipo));
    }
}
