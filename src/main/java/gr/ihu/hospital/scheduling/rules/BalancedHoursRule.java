package gr.ihu.hospital.scheduling.rules;

import java.util.List;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.scheduling.ScheduleState;
import gr.ihu.hospital.scheduling.Violation;

/** Ισορροπία ωρών: προτιμά όποιον έχει λιγότερες ώρες στον μήνα. */
public class BalancedHoursRule extends SoftRule {

    static final int MAX_SPREAD_HOURS = 24;

    public BalancedHoursRule(int weight) {
        super("Ισορροπία ωρών", weight);
    }

    @Override
    protected int penalty(Assignment candidate, ScheduleState state) {
        return state.totalHours(candidate.getStaff());
    }

    @Override
    protected void inspect(ScheduleState state, List<Violation> found) {
        reportSpread(state, found, state::totalHours, MAX_SPREAD_HOURS, "ώρες");
    }
}
