package gr.ihu.hospital.scheduling.rules;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.scheduling.ScheduleState;

/**
 * Τουλάχιστον 11 ώρες ανάπαυσης ανάμεσα σε δύο βάρδιες του ίδιου ατόμου (και καμία επικάλυψη).
 * Προσθήκη πέρα από τους 5 κανόνες του εγγράφου: χωρίς αυτόν ο αλγόριθμος θα έβαζε κάποιον Πρωί + Απόγευμα.
 */
public class MinRestBetweenShiftsRule extends HardRule {

    static final int MIN_REST_HOURS = 11;

    public MinRestBetweenShiftsRule() {
        super("Ανάπαυση 11 ωρών μεταξύ βαρδιών");
    }

    @Override
    protected boolean allows(Assignment candidate, ScheduleState state) {
        for (Assignment existing : state.getAssignments(candidate.getStaff())) {
            if (!restedBetween(existing.getShift(), candidate.getShift())) {
                return false;
            }
        }
        return true;
    }

    private static boolean restedBetween(Shift a, Shift b) {
        return !a.getEnd().plusHours(MIN_REST_HOURS).isAfter(b.getStart())
            || !b.getEnd().plusHours(MIN_REST_HOURS).isAfter(a.getStart());
    }
}
