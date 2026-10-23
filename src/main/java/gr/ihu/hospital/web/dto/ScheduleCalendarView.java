package gr.ihu.hospital.web.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gr.ihu.hospital.domain.schedule.Schedule;
import gr.ihu.hospital.domain.schedule.Shift;

/** UI-05b: το πρόγραμμα ως ημερολόγιο, μία γραμμή ανά ημέρα και μία στήλη ανά (ειδικότητα, βάρδια). */
public final class ScheduleCalendarView {

    private final Map<String, String> columns = new LinkedHashMap<>();
    private final List<Row> rows = new ArrayList<>();

    public ScheduleCalendarView(Schedule schedule) {
        Map<LocalDate, Map<String, Shift>> byDay = new LinkedHashMap<>();
        for (Shift shift : schedule.getShifts()) {
            String slot = slotOf(shift);
            columns.putIfAbsent(slot, shift.getType().getLabel() + " · " + shift.getSpecialty().getName());
            byDay.computeIfAbsent(shift.getDate(), d -> new LinkedHashMap<>()).put(slot, shift);
        }
        for (Map.Entry<LocalDate, Map<String, Shift>> day : byDay.entrySet()) {
            List<Shift> cells = new ArrayList<>();
            for (String slot : columns.keySet()) {
                cells.add(day.getValue().get(slot));
            }
            rows.add(new Row(day.getKey(), cells));
        }
    }

    /** Η τιμή που στέλνει η φόρμα της χειροκίνητης αλλαγής: «specialtyId:SHIFT_TYPE». */
    public static String slotOf(Shift shift) {
        return shift.getSpecialty().getId() + ":" + shift.getType().name();
    }

    /** Τα κλειδιά των στηλών με τη σειρά τους (για να ξέρει κάθε κελί ποια βάρδια είναι). */
    public List<String> getColumnKeys() {
        return new ArrayList<>(columns.keySet());
    }

    public Map<String, String> getColumns() {
        return columns;
    }

    public List<Row> getRows() {
        return rows;
    }

    /** Μία ημέρα του ημερολογίου· τα κελιά ακολουθούν τη σειρά των στηλών (null αν λείπει η βάρδια). */
    public static final class Row {

        private final LocalDate date;
        private final List<Shift> cells;

        Row(LocalDate date, List<Shift> cells) {
            this.date = date;
            this.cells = cells;
        }

        public LocalDate getDate() {
            return date;
        }

        public List<Shift> getCells() {
            return cells;
        }

        public boolean isWeekend() {
            return date.getDayOfWeek().getValue() >= 6;
        }
    }
}
