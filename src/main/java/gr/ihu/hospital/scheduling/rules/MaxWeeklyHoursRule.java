package gr.ihu.hospital.scheduling.rules;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.scheduling.ScheduleState;

/** Μέγιστες ώρες ανά εβδομάδα (παράμετρος του UC-05, π.χ. 48). */
public class MaxWeeklyHoursRule extends HardRule {

    public MaxWeeklyHoursRule() {
        super("Μέγιστες ώρες εβδομάδας");
    }

    // ponytail: η 1η εβδομάδα του μήνα δεν βλέπει τις ώρες του προηγούμενου μήνα.
    @Override
    protected boolean allows(Assignment candidate, ScheduleState state) {
        int hours = state.hoursInWeekOf(candidate.getStaff(), candidate.getShift().getDate());
        return hours + candidate.getShift().getHours() <= state.getContext().getMaxWeeklyHours();
    }
}
