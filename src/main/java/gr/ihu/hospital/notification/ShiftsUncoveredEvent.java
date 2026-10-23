package gr.ihu.hospital.notification;

import gr.ihu.hospital.domain.user.HealthStaff;

/** Γεγονός: μια απουσία άφησε βάρδιες ακάλυπτες (UC-09 βήμα 6 → UC-10 από τον Προϊστάμενο). */
public final class ShiftsUncoveredEvent {

    private final HealthStaff absent;
    private final int shifts;

    public ShiftsUncoveredEvent(HealthStaff absent, int shifts) {
        this.absent = absent;
        this.shifts = shifts;
    }

    public HealthStaff getAbsent() {
        return absent;
    }

    public int getShifts() {
        return shifts;
    }
}
