package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.SlotDisponivelResponse;
import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import ao.gov.embaixada.sgc.enums.TipoAgendamento;
import ao.gov.embaixada.sgc.repository.AgendamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgendamentoSlotConfigTest {

    private AgendamentoSlotConfig slotConfig;
    private AgendamentoRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(AgendamentoRepository.class);
        when(repository.findByDataHoraBetweenAndTipoAndEstadoIn(any(), any(), any(), any()))
                .thenReturn(List.of());
        slotConfig = new AgendamentoSlotConfig(repository);
    }

    @Test
    void shouldGenerateSlotsForDefaultType() {
        // Monday — default 30-min slots, 09-12 + 14-17 = 6h = 12 slots
        LocalDate monday = nextDayOfWeek(DayOfWeek.MONDAY);
        List<SlotDisponivelResponse> slots = slotConfig.getAvailableSlots(monday, TipoAgendamento.PASSAPORTE);

        assertEquals(12, slots.size());
        assertEquals(30, slots.get(0).duracaoMinutos());
        assertEquals(TipoAgendamento.PASSAPORTE, slots.get(0).tipo());
    }

    @Test
    void shouldGenerate60MinSlotsForVisto() {
        // Monday — VISTO uses 60-min slots, 09-12 + 14-17 = 6h = 6 slots
        LocalDate monday = nextDayOfWeek(DayOfWeek.MONDAY);
        List<SlotDisponivelResponse> slots = slotConfig.getAvailableSlots(monday, TipoAgendamento.VISTO);

        assertEquals(6, slots.size());
        assertEquals(60, slots.get(0).duracaoMinutos());
    }

    @Test
    void vistoShouldNotBeAvailableOnThursday() {
        LocalDate thursday = nextDayOfWeek(DayOfWeek.THURSDAY);
        List<SlotDisponivelResponse> slots = slotConfig.getAvailableSlots(thursday, TipoAgendamento.VISTO);

        assertTrue(slots.isEmpty());
    }

    @Test
    void notariadoShouldOnlyBeAvailableOnThursdayAndFriday() {
        LocalDate monday = nextDayOfWeek(DayOfWeek.MONDAY);
        assertTrue(slotConfig.getAvailableSlots(monday, TipoAgendamento.NOTARIADO).isEmpty());

        LocalDate thursday = nextDayOfWeek(DayOfWeek.THURSDAY);
        assertFalse(slotConfig.getAvailableSlots(thursday, TipoAgendamento.NOTARIADO).isEmpty());

        LocalDate friday = nextDayOfWeek(DayOfWeek.FRIDAY);
        assertFalse(slotConfig.getAvailableSlots(friday, TipoAgendamento.NOTARIADO).isEmpty());
    }

    @Test
    void notariadoShouldHave45MinSlots() {
        LocalDate thursday = nextDayOfWeek(DayOfWeek.THURSDAY);
        List<SlotDisponivelResponse> slots = slotConfig.getAvailableSlots(thursday, TipoAgendamento.NOTARIADO);

        assertFalse(slots.isEmpty());
        assertEquals(45, slots.get(0).duracaoMinutos());
    }

    @Test
    void shouldReturnNoSlotsForWeekend() {
        LocalDate saturday = nextDayOfWeek(DayOfWeek.SATURDAY);
        assertTrue(slotConfig.getAvailableSlots(saturday, TipoAgendamento.PASSAPORTE).isEmpty());

        LocalDate sunday = nextDayOfWeek(DayOfWeek.SUNDAY);
        assertTrue(slotConfig.getAvailableSlots(sunday, TipoAgendamento.PASSAPORTE).isEmpty());
    }

    @Test
    void shouldReturnCorrectDuration() {
        assertEquals(30, slotConfig.getDuracaoMinutos(TipoAgendamento.PASSAPORTE));
        assertEquals(60, slotConfig.getDuracaoMinutos(TipoAgendamento.VISTO));
        assertEquals(45, slotConfig.getDuracaoMinutos(TipoAgendamento.NOTARIADO));
    }

    @Test
    void shouldValidateSlotInWorkingHours() {
        LocalDate monday = nextDayOfWeek(DayOfWeek.MONDAY);
        assertTrue(slotConfig.isValidSlot(monday.atTime(9, 0), TipoAgendamento.PASSAPORTE));
        assertTrue(slotConfig.isValidSlot(monday.atTime(14, 0), TipoAgendamento.PASSAPORTE));
        assertFalse(slotConfig.isValidSlot(monday.atTime(13, 0), TipoAgendamento.PASSAPORTE));
        assertFalse(slotConfig.isValidSlot(monday.atTime(17, 0), TipoAgendamento.PASSAPORTE));
    }

    @Test
    void shouldRejectSlotOnWrongDay() {
        LocalDate saturday = nextDayOfWeek(DayOfWeek.SATURDAY);
        assertFalse(slotConfig.isValidSlot(saturday.atTime(10, 0), TipoAgendamento.PASSAPORTE));
    }

    private LocalDate nextDayOfWeek(DayOfWeek day) {
        return LocalDate.now().with(TemporalAdjusters.next(day));
    }
}
