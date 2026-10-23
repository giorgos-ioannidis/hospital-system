package gr.ihu.hospital.scheduling;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr.ihu.hospital.domain.schedule.Assignment;

/** FR-25: υπερωρίες = ώρες πάνω από τις 40 σε κάθε εβδομάδα (Δευτέρα–Κυριακή), αθροισμένες. */
public final class OvertimeCalculator {

    public static final int CONTRACT_WEEKLY_HOURS = 40;

    public int overtimeHours(List<Assignment> assignments) {
        Map<LocalDate, Integer> byWeek = new HashMap<>();
        for (Assignment assignment : assignments) {
            LocalDate monday = assignment.getShift().getDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            byWeek.merge(monday, assignment.getShift().getHours(), Integer::sum);
        }
        int overtime = 0;
        for (int hours : byWeek.values()) {
            overtime += Math.max(0, hours - CONTRACT_WEEKLY_HOURS);
        }
        return overtime;
    }
}
