package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.NotarialFeeResponse;
import ao.gov.embaixada.sgc.enums.TipoServicoNotarial;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@Component
public class NotarialFeeCalculator {

    private final Map<TipoServicoNotarial, BigDecimal> fees = new EnumMap<>(TipoServicoNotarial.class);

    public NotarialFeeCalculator() {
        fees.put(TipoServicoNotarial.PROCURACAO, new BigDecimal("50.00"));
        fees.put(TipoServicoNotarial.LEGALIZACAO, new BigDecimal("30.00"));
        fees.put(TipoServicoNotarial.APOSTILA, new BigDecimal("25.00"));
        fees.put(TipoServicoNotarial.COPIA_CERTIFICADA, new BigDecimal("10.00"));
    }

    public BigDecimal calculateFee(TipoServicoNotarial tipo) {
        return fees.getOrDefault(tipo, BigDecimal.ZERO);
    }

    public boolean isIsento(TipoServicoNotarial tipo) {
        BigDecimal fee = calculateFee(tipo);
        return fee.compareTo(BigDecimal.ZERO) == 0;
    }

    public NotarialFeeResponse getFeeResponse(TipoServicoNotarial tipo) {
        BigDecimal valor = calculateFee(tipo);
        return new NotarialFeeResponse(tipo, valor, "EUR", isIsento(tipo));
    }
}
