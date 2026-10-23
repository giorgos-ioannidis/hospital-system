package gr.ihu.hospital.scheduling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.domain.schedule.Shift;
import gr.ihu.hospital.domain.user.HealthStaff;
import gr.ihu.hospital.scheduling.rules.RuleChain;

/**
 * FR-10 / UC-10: προτείνει αναπληρωτές για ακάλυπτη βάρδια.
 * Υποψήφιοι όσοι περνούν τους σκληρούς κανόνες, ταξινομημένοι με τις λιγότερες ώρες και μετά τις λιγότερες υπερωρίες του μήνα.
 * Αν δεν βρεθεί κανείς στο τμήμα, ψάχνει σε άλλα τμήματα (UC-10).
 */
public final class ReplacementFinder {

    private final RuleChain rules;

    public ReplacementFinder(RuleChain rules) {
        this.rules = rules;
    }

    public List<HealthStaff> find(Shift shift, ScheduleState state, List<HealthStaff> department,
                                  List<HealthStaff> otherDepartments) {
        List<HealthStaff> found = rank(shift, state, department);
        return found.isEmpty() ? rank(shift, state, otherDepartments) : found;
    }

    private List<HealthStaff> rank(Shift shift, ScheduleState state, List<HealthStaff> pool) {
        List<HealthStaff> eligible = new ArrayList<>();
        for (HealthStaff member : pool) {
            if (!shift.isAssigned(member) && rules.evaluate(new Assignment(shift, member), state).isAllowed()) {
                eligible.add(member);
            }
        }
        eligible.sort(Comparator.comparingInt(state::totalHours).thenComparingInt(state::overtimeHours));
        return eligible;
    }
}
