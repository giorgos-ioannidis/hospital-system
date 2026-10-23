package gr.ihu.hospital.scheduling.rules;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.scheduling.ScheduleState;

/** Κανείς σε εγκεκριμένη άδεια, δηλωμένη απουσία ή στις ώρες υποδοχής του (ιατροί). */
public class ApprovedLeaveRule extends HardRule {

    public ApprovedLeaveRule() {
        super("Άδεια / απουσία / ώρες υποδοχής");
    }

    @Override
    protected boolean allows(Assignment candidate, ScheduleState state) {
        Shift shift = candidate.getShift();
        return !state.getContext().isUnavailable(candidate.getStaff(), shift.getStart(), shift.getEnd());
    }
}
