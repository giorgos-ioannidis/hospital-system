package gr.ihu.hospital.scheduling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.domain.schedule.ShiftType;
import gr.ihu.hospital.scheduling.rules.RuleChain;

/**
 * Strategy «δίκαιη»: γεμίζει πρώτα τις βαριές βάρδιες (Σ/Κ, αργίες, εφημερίες),
 * ώστε να μοιραστούν ισότιμα πριν εξαντληθούν οι ώρες της εβδομάδας.
 */
public class FairScheduleGenerator extends GreedyScheduleGenerator {

    public FairScheduleGenerator(RuleChain rules) {
        super(rules);
    }

    @Override
    protected List<Shift> fillOrder(List<Shift> shifts, SchedulingContext context) {
        List<Shift> ordered = new ArrayList<>(shifts);
        ordered.sort(Comparator.comparing((Shift shift) -> !isDemanding(shift, context)));
        return ordered;
    }

    private static boolean isDemanding(Shift shift, SchedulingContext context) {
        return shift.getType() == ShiftType.ON_CALL || context.isWeekendOrHoliday(shift.getDate());
    }
}
