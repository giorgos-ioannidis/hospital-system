package gr.ihu.hospital.domain.schedule;

import java.time.LocalTime;

/** Τύποι βάρδιας με ώρα έναρξης και διάρκεια. Η εφημερία είναι 24ωρη (08:00 → 08:00). */
public enum ShiftType {
    MORNING("Πρωί", LocalTime.of(7, 0), 8),
    AFTERNOON("Απόγευμα", LocalTime.of(15, 0), 8),
    NIGHT("Νύχτα", LocalTime.of(23, 0), 8),
    ON_CALL("Εφημερία", LocalTime.of(8, 0), 24);

    private final String label;
    private final LocalTime start;
    private final int hours;

    ShiftType(String label, LocalTime start, int hours) {
        this.label = label;
        this.start = start;
        this.hours = hours;
    }

    public String getLabel() {
        return label;
    }

    public LocalTime getStart() {
        return start;
    }

    public int getHours() {
        return hours;
    }
}
