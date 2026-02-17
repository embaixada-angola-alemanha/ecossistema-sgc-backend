package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.NotarialFeeResponse;
import ao.gov.embaixada.sgc.enums.TipoServicoNotarial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NotarialFeeCalculatorTest {

    private NotarialFeeCalculator feeCalculator;

    @BeforeEach
    void setUp() {
        feeCalculator = new NotarialFeeCalculator();
    }

    @ParameterizedTest
    @CsvSource({
            "PROCURACAO, 50.00",
            "LEGALIZACAO, 30.00",
            "APOSTILA, 25.00",
            "COPIA_CERTIFICADA, 10.00"
    })
    void shouldCalculateCorrectFee(TipoServicoNotarial tipo, String expectedFee) {
        BigDecimal fee = feeCalculator.calculateFee(tipo);
        assertEquals(0, new BigDecimal(expectedFee).compareTo(fee));
    }

    @Test
    void noServiceTypeShouldBeExempt() {
        for (TipoServicoNotarial tipo : TipoServicoNotarial.values()) {
            assertFalse(feeCalculator.isIsento(tipo),
                    tipo + " should not be exempt");
        }
    }

    @Test
    void shouldReturnFeeResponse() {
        NotarialFeeResponse response = feeCalculator.getFeeResponse(TipoServicoNotarial.PROCURACAO);

        assertEquals(TipoServicoNotarial.PROCURACAO, response.tipo());
        assertEquals(0, new BigDecimal("50.00").compareTo(response.valor()));
        assertEquals("EUR", response.moeda());
        assertFalse(response.isento());
    }

    @Test
    void shouldReturnFeeResponseForAllTypes() {
        for (TipoServicoNotarial tipo : TipoServicoNotarial.values()) {
            NotarialFeeResponse response = feeCalculator.getFeeResponse(tipo);
            assertNotNull(response);
            assertEquals(tipo, response.tipo());
            assertEquals("EUR", response.moeda());
            assertTrue(response.valor().compareTo(BigDecimal.ZERO) > 0);
        }
    }
}
