package gr.ihu.hospital.scheduling.rules;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.scheduling.Evaluation;
import gr.ihu.hospital.scheduling.ScheduleState;
import gr.ihu.hospital.scheduling.Violation;

/** Σκληρός κανόνας: δεν παραβιάζεται ποτέ από τον αλγόριθμο· στη χειροκίνητη αλλαγή δίνει προειδοποίηση. */
public abstract class HardRule extends Rule {

    protected HardRule(String name) {
        super(name);
    }

    @Override
    protected final void apply(Assignment candidate, ScheduleState state, Evaluation result) {
        if (!allows(candidate, state)) {
            String detail = candidate.getStaff().getFullName() + " · " + candidate.getShift().getLabel();
            result.addViolation(new Violation(getName(), true, detail));
        }
    }

    protected abstract boolean allows(Assignment candidate, ScheduleState state);
}
