package gr.ihu.hospital.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Αποτέλεσμα ελέγχου μιας ανάθεσης από το RuleChain: σκληρές παραβιάσεις + ποινή μαλακών κανόνων. */
public final class Evaluation {

    private final List<Violation> violations = new ArrayList<>();
    private int penalty;

    public void addViolation(Violation violation) {
        violations.add(violation);
    }

    public void addPenalty(int points) {
        penalty += points;
    }

    /** Επιτρέπεται αν δεν παραβιάζεται κανένας σκληρός κανόνας. */
    public boolean isAllowed() {
        return violations.isEmpty();
    }

    /** Μικρότερη ποινή = καλύτερος υποψήφιος. */
    public int getPenalty() {
        return penalty;
    }

    public List<Violation> getViolations() {
        return Collections.unmodifiableList(violations);
    }
}
