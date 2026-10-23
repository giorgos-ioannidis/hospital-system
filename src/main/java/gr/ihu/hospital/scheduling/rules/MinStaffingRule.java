package gr.ihu.hospital.scheduling.rules;

import java.util.List;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.scheduling.ScheduleState;
import gr.ihu.hospital.scheduling.Violation;

/** Ελάχιστη στελέχωση ανά βάρδια και ειδικότητα. Ελέγχεται στο επίπεδο του προγράμματος. */
public class MinStaffingRule extends HardRule {

    public MinStaffingRule() {
        super("Ελάχιστη στελέχωση");
    }

    /** Η προσθήκη ατόμου δεν μειώνει ποτέ τη στελέχωση. */
    @Override
    protected boolean allows(Assignment candidate, ScheduleState state) {
        return true;
    }

    @Override
    protected void inspect(ScheduleState state, List<Violation> found) {
        for (Shift shift : state.getShifts()) {
            if (shift.isUnderstaffed()) {
                found.add(new Violation(getName(), true, shift.getLabel() + ": λείπουν " + shift.getMissing()));
            }
        }
    }
}
