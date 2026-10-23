package gr.ihu.hospital.scheduling.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gr.ihu.hospital.domain.schedule.Assignment;
import gr.ihu.hospital.scheduling.Evaluation;
import gr.ihu.hospital.scheduling.ScheduleState;
import gr.ihu.hospital.scheduling.Violation;

/**
 * Chain of Responsibility: συνδέει τους κανόνες και ξεκινά τον έλεγχο από τον πρώτο.
 * Η ίδια αλυσίδα χρησιμοποιείται στην παραγωγή, στη χειροκίνητη αλλαγή (UC-05) και στην αναπλήρωση.
 */
public final class RuleChain {

    /** Βάρος ανά βάρδια Σ/Κ: η δικαιοσύνη στα Σ/Κ μετρά περισσότερο από 1 ώρα διαφορά. */
    static final int WEEKEND_WEIGHT = 100;
    static final int HOURS_WEIGHT = 1;

    private final Rule first;

    public RuleChain(List<Rule> rules) {
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("Η αλυσίδα χρειάζεται τουλάχιστον έναν κανόνα.");
        }
        for (int i = 0; i < rules.size() - 1; i++) {
            rules.get(i).setNext(rules.get(i + 1));
        }
        this.first = rules.get(0);
    }

    /** Οι 6 σκληροί και οι 2 μαλακοί κανόνες της §7.1. */
    public static RuleChain standard() {
        return new RuleChain(new ArrayList<Rule>(Arrays.asList(
            new SpecialtyRule(),
            new ApprovedLeaveRule(),
            new MinRestBetweenShiftsRule(),
            new RestAfterOnCallRule(),
            new MaxWeeklyHoursRule(),
            new MinStaffingRule(),
            new FairWeekendDistributionRule(WEEKEND_WEIGHT),
            new BalancedHoursRule(HOURS_WEIGHT))));
    }

    public Evaluation evaluate(Assignment candidate, ScheduleState state) {
        Evaluation result = new Evaluation();
        first.evaluate(candidate, state, result);
        return result;
    }

    public List<Violation> review(ScheduleState state) {
        List<Violation> found = new ArrayList<>();
        first.review(state, found);
        return found;
    }
}
