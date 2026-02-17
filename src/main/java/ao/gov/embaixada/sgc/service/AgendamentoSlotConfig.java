package ao.gov.embaixada.sgc.service;

import ao.gov.embaixada.sgc.dto.SlotDisponivelResponse;
import ao.gov.embaixada.sgc.entity.Agendamento;
import ao.gov.embaixada.sgc.enums.EstadoAgendamento;
import ao.gov.embaixada.sgc.enums.TipoAgendamento;
import ao.gov.embaixada.sgc.repository.AgendamentoRepository;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
public class AgendamentoSlotConfig {

    private final AgendamentoRepository agendamentoRepository;
    private final Map<TipoAgendamento, SlotRule> rules = new EnumMap<>(TipoAgendamento.class);

    public AgendamentoSlotConfig(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;

        SlotRule defaultRule = new SlotRule(
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                List.of(new TimeBlock(LocalTime.of(9, 0), LocalTime.of(12, 0)),
                        new TimeBlock(LocalTime.of(14, 0), LocalTime.of(17, 0))),
                30);

        for (TipoAgendamento tipo : TipoAgendamento.values()) {
            rules.put(tipo, defaultRule);
        }

        // VISTO: Mon-Wed only, 60-min slots (interview)
        rules.put(TipoAgendamento.VISTO, new SlotRule(
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY),
                List.of(new TimeBlock(LocalTime.of(9, 0), LocalTime.of(12, 0)),
                        new TimeBlock(LocalTime.of(14, 0), LocalTime.of(17, 0))),
                60));

        // NOTARIADO: Thu-Fri only, 45-min slots
        rules.put(TipoAgendamento.NOTARIADO, new SlotRule(
                Set.of(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                List.of(new TimeBlock(LocalTime.of(9, 0), LocalTime.of(12, 0)),
                        new TimeBlock(LocalTime.of(14, 0), LocalTime.of(17, 0))),
                45));
    }

    public int getDuracaoMinutos(TipoAgendamento tipo) {
        return rules.get(tipo).duracaoMinutos();
    }

    public List<SlotDisponivelResponse> getAvailableSlots(LocalDate date, TipoAgendamento tipo) {
        SlotRule rule = rules.get(tipo);

        if (!rule.workingDays().contains(date.getDayOfWeek())) {
            return List.of();
        }

        List<LocalDateTime> allSlots = new ArrayList<>();
        for (TimeBlock block : rule.timeBlocks()) {
            LocalTime time = block.start();
            while (time.plusMinutes(rule.duracaoMinutos()).compareTo(block.end()) <= 0) {
                allSlots.add(LocalDateTime.of(date, time));
                time = time.plusMinutes(rule.duracaoMinutos());
            }
        }

        // Subtract already-booked slots
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        List<EstadoAgendamento> activeStates = List.of(
                EstadoAgendamento.PENDENTE, EstadoAgendamento.CONFIRMADO);

        List<Agendamento> booked = agendamentoRepository
                .findByDataHoraBetweenAndTipoAndEstadoIn(dayStart, dayEnd, tipo, activeStates);

        Set<LocalDateTime> bookedTimes = new HashSet<>();
        for (Agendamento a : booked) {
            bookedTimes.add(a.getDataHora());
        }

        return allSlots.stream()
                .filter(slot -> !bookedTimes.contains(slot))
                .map(slot -> new SlotDisponivelResponse(slot, rule.duracaoMinutos(), tipo))
                .toList();
    }

    public boolean isValidSlot(LocalDateTime dateTime, TipoAgendamento tipo) {
        SlotRule rule = rules.get(tipo);
        if (!rule.workingDays().contains(dateTime.getDayOfWeek())) {
            return false;
        }
        LocalTime time = dateTime.toLocalTime();
        for (TimeBlock block : rule.timeBlocks()) {
            if (!time.isBefore(block.start()) &&
                    !time.plusMinutes(rule.duracaoMinutos()).isAfter(block.end())) {
                return true;
            }
        }
        return false;
    }

    public record SlotRule(Set<DayOfWeek> workingDays, List<TimeBlock> timeBlocks, int duracaoMinutos) {}
    public record TimeBlock(LocalTime start, LocalTime end) {}
}
