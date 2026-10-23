package gr.ihu.hospital.scheduling.rules;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.scheduling.ScheduleState;

/** Σωστή ειδικότητα για τη βάρδια. */
public class SpecialtyRule extends HardRule {

    public SpecialtyRule() {
        super("Σωστή ειδικότητα");
    }

    @Override
    protected boolean allows(Assignment candidate, ScheduleState state) {
        return candidate.getStaff().hasSpecialty(candidate.getShift().getSpecialty());
    }
}
