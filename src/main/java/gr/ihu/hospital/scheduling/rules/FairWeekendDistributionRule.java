package gr.ihu.hospital.scheduling.rules;

import java.util.List;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.scheduling.ScheduleState;
import gr.ihu.hospital.scheduling.Violation;

/** Δίκαιη κατανομή Σαββατοκύριακων/αργιών: διαφορά ≤ 1 βάρδια ανάμεσα σε μέλη της ίδιας ειδικότητας. */
public class FairWeekendDistributionRule extends SoftRule {

    static final int MAX_DIFFERENCE = 1;

    public FairWeekendDistributionRule(int weight) {
        super("Δίκαιη κατανομή Σ/Κ και αργιών", weight);
    }

    @Override
    protected int penalty(Assignment candidate, ScheduleState state) {
        boolean weekend = state.getContext().isWeekendOrHoliday(candidate.getShift().getDate());
        return weekend ? state.weekendOrHolidayShifts(candidate.getStaff()) : 0;
    }

    @Override
    protected void inspect(ScheduleState state, List<Violation> found) {
        reportSpread(state, found, state::weekendOrHolidayShifts, MAX_DIFFERENCE, "βάρδιες Σ/Κ-αργιών");
    }
}
