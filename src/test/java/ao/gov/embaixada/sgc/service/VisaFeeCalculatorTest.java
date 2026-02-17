package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.VisaFeeResponse;
import ao.gov.embaixada.sgc.enums.TipoVisto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class VisaFeeCalculatorTest {

    private VisaFeeCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new VisaFeeCalculator();
    }

    @ParameterizedTest
    @CsvSource({
            "TURISTA, 60.00",
            "NEGOCIO, 100.00",
            "TRABALHO, 150.00",
            "ESTUDANTE, 40.00",
            "TRANSITO, 30.00",
            "FAMILIAR, 60.00",
            "DIPLOMATICO, 0",
            "CORTESIA, 0"
    })
    void shouldCalculateCorrectFee(TipoVisto tipo, BigDecimal expectedFee) {
        assertEquals(0, expectedFee.compareTo(calculator.calculateFee(tipo)));
    }

    @Test
    void diplomaticoShouldBeIsento() {
        assertTrue(calculator.isIsento(TipoVisto.DIPLOMATICO));
    }

    @Test
    void cortesiaShouldBeIsento() {
        assertTrue(calculator.isIsento(TipoVisto.CORTESIA));
    }

    @Test
    void turistaShouldNotBeIsento() {
        assertFalse(calculator.isIsento(TipoVisto.TURISTA));
    }

    @Test
    void shouldReturnFeeResponse() {
        VisaFeeResponse response = calculator.getFeeResponse(TipoVisto.TRABALHO);
        assertEquals(TipoVisto.TRABALHO, response.tipo());
        assertEquals(0, new BigDecimal("150.00").compareTo(response.valor()));
        assertEquals("EUR", response.moeda());
        assertFalse(response.isento());
    }

    @Test
    void shouldReturnIsentoFeeResponse() {
        VisaFeeResponse response = calculator.getFeeResponse(TipoVisto.DIPLOMATICO);
        assertEquals(TipoVisto.DIPLOMATICO, response.tipo());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.valor()));
        assertTrue(response.isento());
    }
}
