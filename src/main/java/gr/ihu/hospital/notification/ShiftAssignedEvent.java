package gr.ihu.hospital.notification;

import gr.ihu.hospital.domain.schedule.Assignment;

/** Γεγονός: ένας εργαζόμενος ορίστηκε αναπληρωτής σε βάρδια (UC-10). */
public final class ShiftAssignedEvent {

    private final Assignment assignment;

    public ShiftAssignedEvent(Assignment assignment) {
        this.assignment = assignment;
    }

    public Assignment getAssignment() {
        return assignment;
    }
}
