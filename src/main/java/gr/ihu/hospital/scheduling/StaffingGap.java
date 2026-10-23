package gr.ihu.hospital.scheduling;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import gr.ihu.hospital.domain.organization.Specialty;
import gr.ihu.hospital.domain.schedule.ShiftType;

/** Βάρδια που θα έπεφτε κάτω από την ελάχιστη στελέχωση αν εγκριθεί μια άδεια (FR-07). */
public final class StaffingGap {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM");

    private final LocalDate date;
    private final ShiftType type;
    private final Specialty specialty;

    public StaffingGap(LocalDate date, ShiftType type, Specialty specialty) {
        this.date = date;
        this.type = type;
        this.specialty = specialty;
    }

    public LocalDate getDate() {
        return date;
    }

    public ShiftType getType() {
        return type;
    }

    public String getLabel() {
        return date.format(DAY) + " " + type.getLabel() + " (" + specialty.getName() + ")";
    }
}
